package edu.pkmon.capture.domain;

import java.time.Instant;

/** 一次作答请求。requestId 由客户端为一次业务操作生成，重试沿用同一个。 */
public record AnswerAttempt(
        String attemptId,
        String userId,
        String challengeId,
        String requestId,
        String optionId,
        AnswerResult result,
        Instant submittedAt) {
}
