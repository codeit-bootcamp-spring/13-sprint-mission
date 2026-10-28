package com.sprint.mission.discodeit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.sse.DefaultSseEventPublisher;
import com.sprint.mission.discodeit.sse.InMemorySseMessageRepository;
import com.sprint.mission.discodeit.sse.RedisSseEventSubscriber;
import com.sprint.mission.discodeit.sse.RedisSseMessageRepository;
import com.sprint.mission.discodeit.sse.SseConnectionRepository;
import com.sprint.mission.discodeit.sse.SseEventPublisher;
import com.sprint.mission.discodeit.sse.SseMessageDispatcher;
import com.sprint.mission.discodeit.sse.SseMessageReader;
import com.sprint.mission.discodeit.sse.SseMessageRepository;
import com.sprint.mission.discodeit.sse.SseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * 실제 Spring 컨텍스트에서 설정값에 따라 SSE 저장소가 하나만 선택되는지 확인한다.
 * RedisTemplate은 Mock으로 두며 이 테스트는 Redis 접속·Lua 실행이 아닌 빈 구성 계약을 검증한다.
 */
@DisplayName("SSE 저장소 선택 및 공통 빈 연결 테스트")
class SseConfigurationTest {
    // Redis listener container는 시작하지 않는다. 실제 Redis 동작은 별도 통합 테스트의 책임이다.
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(InMemorySseMessageRepository.class, RedisSseMessageRepository.class,
                    DefaultSseEventPublisher.class, SseMessageReader.class, SseMessageDispatcher.class,
                    SseService.class, SseConnectionRepository.class, RedisSseEventSubscriber.class)
            .withBean(ObjectMapper.class, ObjectMapper::new);

    @ParameterizedTest
    @ValueSource(strings = {"", "in-memory"})
    @DisplayName("타입 생략 또는 in-memory 선택 시 Redis 없이 공통 컴포넌트를 연결한다")
    void inMemoryOrMissingType_registersOnlyInMemoryRepository(String type) {
        ApplicationContextRunner configured = type.isEmpty() ? runner
                : runner.withPropertyValues("discodeit.security.jwt.registry-type=" + type);

        configured.run(context -> {
            assertThat(context).hasNotFailed()
                    .hasSingleBean(SseMessageRepository.class)
                    .hasSingleBean(InMemorySseMessageRepository.class)
                    .doesNotHaveBean(RedisSseMessageRepository.class)
                    .doesNotHaveBean(RedisSseEventSubscriber.class)
                    .doesNotHaveBean(StringRedisTemplate.class)
                    .hasSingleBean(SseEventPublisher.class)
                    .hasSingleBean(DefaultSseEventPublisher.class)
                    .hasSingleBean(SseMessageReader.class)
                    .hasSingleBean(SseMessageDispatcher.class)
                    .hasSingleBean(SseService.class);
        });
    }

    @Test
    @DisplayName("redis 선택 시 Redis 저장소 하나와 공통 컴포넌트를 연결한다")
    void redis_registersOnlyRedisRepository() {
        StringRedisTemplate template = mock(StringRedisTemplate.class);

        runner.withPropertyValues("discodeit.security.jwt.registry-type=redis")
                .withBean(StringRedisTemplate.class, () -> template)
                .run(context -> {
                    assertThat(context).hasNotFailed()
                            .hasSingleBean(SseMessageRepository.class)
                            .hasSingleBean(RedisSseMessageRepository.class)
                            .doesNotHaveBean(InMemorySseMessageRepository.class)
                            .hasSingleBean(RedisSseEventSubscriber.class)
                            .hasSingleBean(SseEventPublisher.class)
                            .hasSingleBean(DefaultSseEventPublisher.class)
                            .hasSingleBean(SseMessageReader.class)
                            .hasSingleBean(SseMessageDispatcher.class)
                            .hasSingleBean(SseService.class);
                });
    }
}
