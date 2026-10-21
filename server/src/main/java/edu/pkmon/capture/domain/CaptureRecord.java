package edu.pkmon.capture.domain;

import java.time.Instant;

/** 已结算、已持久化的收录结果；入账的唯一物证。 */
public record CaptureRecord(
        String captureRecordId,
        String userId,
        String spawnId,
        String challengeId,
        Instant capturedAt) {
}
