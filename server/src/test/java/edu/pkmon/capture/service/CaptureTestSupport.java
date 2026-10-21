package edu.pkmon.capture.service;

import edu.pkmon.capture.port.FixtureCatalog;
import edu.pkmon.capture.repository.Repositories.AnswerAttemptRepository;
import edu.pkmon.capture.repository.Repositories.CaptureRecordRepository;
import edu.pkmon.capture.repository.Repositories.ChallengeRepository;
import edu.pkmon.capture.repository.Repositories.SpawnRepository;
import edu.pkmon.capture.repository.memory.InMemoryAnswerAttemptRepository;
import edu.pkmon.capture.repository.memory.InMemoryCaptureRecordRepository;
import edu.pkmon.capture.repository.memory.InMemoryChallengeRepository;
import edu.pkmon.capture.repository.memory.InMemorySpawnRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * 测试装配：固定时钟 + 内存仓储替身 + 固定题库替身。
 * 这里的内存仓储只用于业务规则测试，不代表真实持久化能力。
 */
final class CaptureTestSupport {

    final TestClock clock;
    final FixtureCatalog catalog = new FixtureCatalog();
    final SpawnRepository spawns = new InMemorySpawnRepository();
    final ChallengeRepository challenges = new InMemoryChallengeRepository();
    final AnswerAttemptRepository attempts = new InMemoryAnswerAttemptRepository();
    final CaptureRecordRepository records = new InMemoryCaptureRecordRepository();

    final SpawnService spawnService;
    final CaptureService captureService;
    final CollectionService collectionService;

    CaptureTestSupport(Instant start) {
        this.clock = new TestClock(start);
        this.spawnService = new SpawnService(clock, spawns, true);
        this.captureService = new CaptureService(clock, spawns, challenges, attempts, records,
                catalog, catalog);
        this.collectionService = new CollectionService(spawns, records, catalog);
    }

    /** 可推进的固定时钟：不 sleep，不依赖真实时间。 */
    static final class TestClock extends Clock {

        private Instant now;

        TestClock(Instant start) {
            this.now = start;
        }

        void advance(Duration duration) {
            this.now = this.now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
