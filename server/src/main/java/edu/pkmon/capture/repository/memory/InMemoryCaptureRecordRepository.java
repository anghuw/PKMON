package edu.pkmon.capture.repository.memory;

import edu.pkmon.capture.domain.CaptureRecord;
import edu.pkmon.capture.repository.DuplicateRecordException;
import edu.pkmon.capture.repository.Repositories.CaptureRecordRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** 内存替身：用 ConcurrentMap.putIfAbsent 模拟 (userId, spawnId) 唯一约束。 */
public final class InMemoryCaptureRecordRepository implements CaptureRecordRepository {

    private final ConcurrentMap<String, CaptureRecord> byUserAndSpawn = new ConcurrentHashMap<>();

    private static String key(String userId, String spawnId) {
        return userId + "\u0000" + spawnId;
    }

    @Override
    public Optional<CaptureRecord> findByUserAndSpawn(String userId, String spawnId) {
        if (userId == null || spawnId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(byUserAndSpawn.get(key(userId, spawnId)));
    }

    @Override
    public List<CaptureRecord> findByUser(String userId) {
        return byUserAndSpawn.values().stream()
                .filter(r -> r.userId().equals(userId))
                .sorted(Comparator.comparing(CaptureRecord::capturedAt))
                .toList();
    }

    @Override
    public CaptureRecord save(CaptureRecord record) {
        String key = key(record.userId(), record.spawnId());
        if (byUserAndSpawn.putIfAbsent(key, record) != null) {
            throw new DuplicateRecordException("capture_record (user_id, spawn_id) 已存在");
        }
        return record;
    }
}
