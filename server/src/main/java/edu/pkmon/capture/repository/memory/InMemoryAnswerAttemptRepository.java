package edu.pkmon.capture.repository.memory;

import edu.pkmon.capture.domain.AnswerAttempt;
import edu.pkmon.capture.repository.DuplicateRecordException;
import edu.pkmon.capture.repository.Repositories.AnswerAttemptRepository;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** 内存替身：用 ConcurrentMap.putIfAbsent 模拟 (userId, requestId) 唯一约束。 */
public final class InMemoryAnswerAttemptRepository implements AnswerAttemptRepository {

    private final ConcurrentMap<String, AnswerAttempt> byUserAndRequest = new ConcurrentHashMap<>();

    private static String key(String userId, String requestId) {
        return userId + "\u0000" + requestId;
    }

    @Override
    public Optional<AnswerAttempt> findByUserAndRequestId(String userId, String requestId) {
        if (userId == null || requestId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byUserAndRequest.get(key(userId, requestId)));
    }

    @Override
    public Optional<AnswerAttempt> findLatestByUserAndChallenge(String userId, String challengeId) {
        return byUserAndRequest.values().stream()
                .filter(a -> a.userId().equals(userId))
                .filter(a -> a.challengeId().equals(challengeId))
                .max(Comparator.comparing(AnswerAttempt::submittedAt));
    }

    @Override
    public AnswerAttempt save(AnswerAttempt attempt) {
        String key = key(attempt.userId(), attempt.requestId());
        if (byUserAndRequest.putIfAbsent(key, attempt) != null) {
            throw new DuplicateRecordException("answer_attempt (user_id, request_id) 已存在");
        }
        return attempt;
    }
}
