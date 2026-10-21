package edu.pkmon.capture.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.pkmon.capture.domain.SpawnSourceType;
import edu.pkmon.capture.dto.CaptureDtos.AnswerResponse;
import edu.pkmon.capture.dto.CaptureDtos.ChallengeResponse;
import edu.pkmon.capture.dto.CaptureDtos.ChallengeStatusResponse;
import edu.pkmon.capture.dto.CaptureDtos.SubmitAnswerCommand;
import edu.pkmon.capture.error.ApiErrorCode;
import edu.pkmon.capture.error.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 答题捕获业务规则测试：固定时钟，不 sleep。用例编号对应 design/test-cases.csv。 */
class CaptureServiceTest {

    private static final Instant T0 = Instant.parse("2026-10-25T12:00:00Z");
    private static final String USER = "u-1";
    private static final String OTHER = "u-2";

    private CaptureTestSupport support;
    private String spawnId;
    private String challengeId;

    @BeforeEach
    void setUp() {
        support = new CaptureTestSupport(T0);
        spawnId = support.spawnService.createDevSpawn(USER, "library_entrance", "siege_lion").spawnId();
        challengeId = support.captureService.startChallenge(USER, spawnId).challengeId();
    }

    @Test
    @DisplayName("TC-01 答对：产生唯一收录并置 CAPTURED")
    void correctAnswerCreatesSingleRecord() {
        AnswerResponse response = submit(USER, "req-1", "A");

        assertEquals("CORRECT", response.result());
        assertNotNull(response.captureRecordId());
        assertEquals("CAPTURED", status().status());
        assertEquals(1, support.collectionService.summary(USER).captureCount());
        assertEquals(1, support.collectionService.summary(USER).collectedSpeciesCount());
    }

    @Test
    @DisplayName("TC-02 同 requestId 同内容：回放原结果，不新增记录")
    void sameRequestIdSameContentReplays() {
        AnswerResponse first = submit(USER, "req-1", "A");
        AnswerResponse second = submit(USER, "req-1", "A");

        assertEquals(first, second);
        assertEquals(1, support.collectionService.summary(USER).captureCount());
    }

    @Test
    @DisplayName("TC-03 同 requestId 不同内容：冲突且不改原记录")
    void sameRequestIdDifferentContentConflicts() {
        AnswerResponse first = submit(USER, "req-1", "A");

        ApiException error = assertThrows(ApiException.class, () -> submit(USER, "req-1", "B"));

        assertEquals(ApiErrorCode.CONFLICT_REQUEST, error.errorCode());
        assertEquals("CAPTURED", status().status());
        assertEquals(first.captureRecordId(), status().captureRecordId());
        assertEquals(1, support.collectionService.summary(USER).captureCount());
    }

    @Test
    @DisplayName("TC-04 答错：不消耗挑战，可用新 requestId 重试后捕获")
    void wrongAnswerAllowsRetry() {
        AnswerResponse wrong = submit(USER, "req-1", "B");

        assertEquals("WRONG", wrong.result());
        assertNull(wrong.captureRecordId());
        assertEquals("OPEN", status().status());
        assertEquals(0, support.collectionService.summary(USER).captureCount());

        AnswerResponse correct = submit(USER, "req-2", "A");
        assertEquals("CORRECT", correct.result());
        assertEquals(1, support.collectionService.summary(USER).captureCount());
    }

    @Test
    @DisplayName("TC-05 无效选项：拒绝且不消耗挑战，也不产生作答记录")
    void invalidOptionDoesNotConsumeChallenge() {
        ApiException error = assertThrows(ApiException.class, () -> submit(USER, "req-bad", "Z"));

        assertEquals(ApiErrorCode.INVALID_PARAM, error.errorCode());
        assertEquals("OPEN", status().status());
        assertNull(status().lastAttempt());
        assertEquals(0, support.collectionService.summary(USER).captureCount());

        assertEquals("CORRECT", submit(USER, "req-1", "A").result());
    }

    @Test
    @DisplayName("TC-06a 到期前 1 秒仍可作答")
    void stillValidOneSecondBeforeExpiry() {
        support.clock.advance(Duration.ofSeconds(299));

        assertEquals("CORRECT", submit(USER, "req-1", "A").result());
    }

    @Test
    @DisplayName("TC-06b 到期当刻即拒绝，且不产生收录")
    void expiryInstantIsRejected() {
        support.clock.advance(Duration.ofMinutes(5));

        ApiException error = assertThrows(ApiException.class, () -> submit(USER, "req-1", "A"));

        assertEquals(ApiErrorCode.EXPIRED, error.errorCode());
        assertEquals("EXPIRED", status().status());
        assertEquals(0, support.collectionService.summary(USER).captureCount());
    }

    @Test
    @DisplayName("TC-11 已处理请求在过期后仍回放原结果，不报过期")
    void processedRequestReplaysAfterExpiry() {
        AnswerResponse first = submit(USER, "req-1", "A");

        support.clock.advance(Duration.ofMinutes(30));
        AnswerResponse replay = submit(USER, "req-1", "A");

        assertEquals(first, replay);
        assertEquals("CORRECT", replay.result());
        assertEquals(first.captureRecordId(), replay.captureRecordId());
    }

    @Test
    @DisplayName("TC-12 同一遭遇复用未过期挑战，不新建")
    void reusesOpenChallenge() {
        String again = support.captureService.startChallenge(USER, spawnId).challengeId();

        assertEquals(challengeId, again);
    }

    @Test
    @DisplayName("已收录的遭遇不能再发起挑战")
    void capturedSpawnCannotStartChallengeAgain() {
        submit(USER, "req-1", "A");

        ApiException error = assertThrows(ApiException.class,
                () -> support.captureService.startChallenge(USER, spawnId));

        assertEquals(ApiErrorCode.ALREADY_CAPTURED, error.errorCode());
    }

    @Test
    @DisplayName("遭遇过期后不能再发起挑战")
    void expiredSpawnCannotStartChallenge() {
        support.clock.advance(Duration.ofMinutes(10));

        ApiException error = assertThrows(ApiException.class,
                () -> support.captureService.startChallenge(USER, spawnId));

        assertEquals(ApiErrorCode.EXPIRED, error.errorCode());
    }

    @Test
    @DisplayName("TC-09 他人资源不可读也不可作答，且不影响他人计数")
    void otherUserCannotReadOrAnswer() {
        submit(USER, "req-1", "A");

        ApiException readError = assertThrows(ApiException.class,
                () -> support.captureService.status(OTHER, challengeId));
        ApiException answerError = assertThrows(ApiException.class, () -> submit(OTHER, "req-x", "A"));

        assertEquals(ApiErrorCode.NOT_FOUND, readError.errorCode());
        assertEquals(ApiErrorCode.NOT_FOUND, answerError.errorCode());
        assertEquals(0, support.collectionService.summary(OTHER).captureCount());
    }

    @Test
    @DisplayName("TC-10 取题响应不含任何答案字段")
    void challengeResponseNeverDisclosesAnswer() throws Exception {
        ChallengeResponse response = support.captureService.startChallenge(USER, spawnId);
        ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
        String json = mapper.writeValueAsString(response);

        assertFalse(json.contains("correctOptionId"));
        assertFalse(json.contains("\"answer\""));
        assertFalse(json.contains("\"correct\""));
        assertTrue(json.contains("\"optionId\""));
        assertEquals(3, response.options().size());
    }

    @Test
    @DisplayName("开发遭遇只产生机会：不产生收录也不增加计数")
    void devSpawnIsNotAReward() {
        assertEquals(SpawnSourceType.DEV_SIMULATED,
                support.spawnService.createDevSpawn(USER, "library_entrance", "siege_lion").sourceType());
        assertEquals(0, support.collectionService.summary(USER).captureCount());
        assertEquals(0, support.collectionService.summary(USER).collectedSpeciesCount());
    }

    @Test
    @DisplayName("参数缺失时返回参数错误")
    void blankFieldsAreRejected() {
        ApiException error = assertThrows(ApiException.class,
                () -> support.captureService.submit(USER, new SubmitAnswerCommand(challengeId, " ", "A")));

        assertEquals(ApiErrorCode.INVALID_PARAM, error.errorCode());
    }

    private AnswerResponse submit(String userId, String requestId, String optionId) {
        return support.captureService.submit(userId,
                new SubmitAnswerCommand(challengeId, requestId, optionId));
    }

    private ChallengeStatusResponse status() {
        return support.captureService.status(USER, challengeId);
    }
}
