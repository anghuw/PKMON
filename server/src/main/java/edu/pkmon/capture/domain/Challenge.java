package edu.pkmon.capture.domain;

import java.time.Instant;

/** 问答挑战：一次遭遇的一个问答窗口。status 可变更，故用类而非 record。 */
public final class Challenge {

    private final String challengeId;
    private final String userId;
    private final String spawnId;
    private final String questionId;
    private final Instant expiresAt;
    private ChallengeStatus status;

    public Challenge(String challengeId, String userId, String spawnId, String questionId,
                     ChallengeStatus status, Instant expiresAt) {
        this.challengeId = challengeId;
        this.userId = userId;
        this.spawnId = spawnId;
        this.questionId = questionId;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    public String challengeId() {
        return challengeId;
    }

    public String userId() {
        return userId;
    }

    public String spawnId() {
        return spawnId;
    }

    public String questionId() {
        return questionId;
    }

    public ChallengeStatus status() {
        return status;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public void markCaptured() {
        this.status = ChallengeStatus.CAPTURED;
    }

    public void markExpired() {
        this.status = ChallengeStatus.EXPIRED;
    }

    /** 到期当刻即视为过期。 */
    public boolean expiredAt(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public boolean openAt(Instant now) {
        return status == ChallengeStatus.OPEN && !expiredAt(now);
    }
}
