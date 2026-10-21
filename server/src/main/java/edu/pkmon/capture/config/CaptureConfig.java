package edu.pkmon.capture.config;

import edu.pkmon.capture.error.ApiExceptionHandler;
import edu.pkmon.capture.port.CurrentUserProvider;
import edu.pkmon.capture.port.DevHeaderCurrentUserProvider;
import edu.pkmon.capture.port.FixtureCatalog;
import edu.pkmon.capture.port.UnavailableCurrentUserProvider;
import edu.pkmon.capture.repository.Repositories.AnswerAttemptRepository;
import edu.pkmon.capture.repository.Repositories.CaptureRecordRepository;
import edu.pkmon.capture.repository.Repositories.ChallengeRepository;
import edu.pkmon.capture.repository.Repositories.SpawnRepository;
import edu.pkmon.capture.repository.memory.InMemoryAnswerAttemptRepository;
import edu.pkmon.capture.repository.memory.InMemoryCaptureRecordRepository;
import edu.pkmon.capture.repository.memory.InMemoryChallengeRepository;
import edu.pkmon.capture.repository.memory.InMemorySpawnRepository;
import edu.pkmon.capture.service.CaptureService;
import edu.pkmon.capture.service.CollectionService;
import edu.pkmon.capture.service.SpawnService;
import edu.pkmon.capture.web.CaptureController;
import edu.pkmon.capture.web.CollectionController;
import edu.pkmon.capture.web.DevSpawnController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

/**
 * 本模块的装配点。启动类 PkmonApplication 位于 edu.pkmon.demo 且由胡豪主责，
 * 其组件扫描范围不含 edu.pkmon.capture，因此本模块通过 Spring Boot 自动配置注册自己
 * （见 META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports），
 * 不修改启动类。若日后启动类改为扫描 edu.pkmon，本类所有 @Bean 会因
 * @ConditionalOnMissingBean 自动让位，不会产生重复定义。
 *
 * 三个开关默认是“开发期形态”，正式环境必须由公共配置显式关闭：
 *   pkmon.capture.dev-user-header     true 时用 X-Dev-User 头当身份（临时替身，非认证）
 *   pkmon.capture.dev-spawn-enabled   true 时开放 /dev/spawns
 *   pkmon.capture.in-memory-store     true 时用内存仓储替身（进程重启即丢失）
 */
@AutoConfiguration
public class CaptureConfig {

    @Bean
    @ConditionalOnMissingBean
    public Clock captureClock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnMissingBean
    public FixtureCatalog fixtureCatalog() {
        return new FixtureCatalog();
    }

    @Bean
    @ConditionalOnMissingBean
    public CurrentUserProvider currentUserProvider(
            @Value("${pkmon.capture.dev-user-header:true}") boolean devUserHeader) {
        return devUserHeader ? new DevHeaderCurrentUserProvider() : new UnavailableCurrentUserProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "pkmon.capture.in-memory-store", havingValue = "true", matchIfMissing = true)
    public SpawnRepository spawnRepository() {
        return new InMemorySpawnRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "pkmon.capture.in-memory-store", havingValue = "true", matchIfMissing = true)
    public ChallengeRepository challengeRepository() {
        return new InMemoryChallengeRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "pkmon.capture.in-memory-store", havingValue = "true", matchIfMissing = true)
    public AnswerAttemptRepository answerAttemptRepository() {
        return new InMemoryAnswerAttemptRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "pkmon.capture.in-memory-store", havingValue = "true", matchIfMissing = true)
    public CaptureRecordRepository captureRecordRepository() {
        return new InMemoryCaptureRecordRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    public SpawnService spawnService(Clock clock, SpawnRepository spawnRepository,
                                     @Value("${pkmon.capture.dev-spawn-enabled:true}") boolean devSpawnEnabled) {
        return new SpawnService(clock, spawnRepository, devSpawnEnabled);
    }

    @Bean
    @ConditionalOnMissingBean
    public CaptureService captureService(Clock clock, SpawnRepository spawnRepository,
                                         ChallengeRepository challengeRepository,
                                         AnswerAttemptRepository answerAttemptRepository,
                                         CaptureRecordRepository captureRecordRepository,
                                         FixtureCatalog fixtureCatalog) {
        return new CaptureService(clock, spawnRepository, challengeRepository,
                answerAttemptRepository, captureRecordRepository, fixtureCatalog, fixtureCatalog);
    }

    @Bean
    @ConditionalOnMissingBean
    public CollectionService collectionService(SpawnRepository spawnRepository,
                                               CaptureRecordRepository captureRecordRepository,
                                               FixtureCatalog fixtureCatalog) {
        return new CollectionService(spawnRepository, captureRecordRepository, fixtureCatalog);
    }

    @Bean
    @ConditionalOnMissingBean
    public DevSpawnController devSpawnController(SpawnService spawnService,
                                                 CurrentUserProvider currentUserProvider) {
        return new DevSpawnController(spawnService, currentUserProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public CaptureController captureController(CaptureService captureService,
                                               CurrentUserProvider currentUserProvider) {
        return new CaptureController(captureService, currentUserProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public CollectionController collectionController(CollectionService collectionService,
                                                     CurrentUserProvider currentUserProvider) {
        return new CollectionController(collectionService, currentUserProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiExceptionHandler apiExceptionHandler() {
        return new ApiExceptionHandler();
    }
}
