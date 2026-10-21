package edu.pkmon.capture.domain;

/** 挑战状态。注意：WRONG 不是状态，是作答结果，故不在此枚举中。 */
public enum ChallengeStatus {
    OPEN,
    CAPTURED,
    EXPIRED
}
