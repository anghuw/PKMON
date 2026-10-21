package edu.pkmon.capture.domain;

import java.time.Instant;

/** 遭遇机会：某区域某段时间内允许发起挑战的机会，不等于奖励。 */
public record Spawn(
        String spawnId,
        String userId,
        String regionId,
        String speciesId,
        SpawnSourceType sourceType,
        Instant expiresAt) {

    /** 有效期当刻即视为不可用，与 TC-06 的边界约定一致。 */
    public boolean usableAt(Instant now) {
        return now.isBefore(expiresAt);
    }
}
