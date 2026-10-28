package com.sprint.mission.discodeit.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.DefaultMessage;
import org.springframework.data.redis.connection.Message;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

import static com.sprint.mission.discodeit.sse.SseTestFixtures.objectMapper;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 실제 구독자와 ObjectMapper로 Redis 메시지 본문을 공통 이벤트로 복원한다.
 * 로컬 이벤트 발행만 Mock으로 관찰하며 실제 Redis 네트워크나 구독 재연결은 실행하지 않는다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Redis SSE 구독 메시지 변환 단위 테스트")
class RedisSseEventSubscriberTest {
    @Mock ApplicationEventPublisher eventPublisher;
    private final ObjectMapper mapper = objectMapper();
    private RedisSseEventSubscriber subscriber;

    @BeforeEach
    void setUp() {
        subscriber = new RedisSseEventSubscriber(mapper, eventPublisher);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("수신한 JSON의 ID와 수신 범위 및 데이터 JSON을 그대로 복원하여 발행한다")
    void onMessage_publishesCommonMessage(boolean broadcast) throws Exception {
        SseMessage expected = new SseMessage(UUID.randomUUID(), "channels.updated",
                "{\"name\":\"채널\"}", broadcast ? Set.of() : Set.of(UUID.randomUUID()), broadcast);
        Message redisMessage = new DefaultMessage(
                RedisSseMessageRepository.SSE_EVENTS_TOPIC.getBytes(StandardCharsets.UTF_8),
                mapper.writeValueAsBytes(expected));

        subscriber.onMessage(redisMessage, null);

        ArgumentCaptor<SseMessage> captured = ArgumentCaptor.forClass(SseMessage.class);
        verify(eventPublisher).publishEvent(captured.capture());
        assertThat(captured.getValue()).isEqualTo(expected);
    }

    @Test
    @DisplayName("잘못된 JSON이면 Spring 이벤트를 발행하지 않는다")
    void onMessage_rejectsMalformedJson() {
        Message redisMessage = new DefaultMessage(new byte[0], "{invalid".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> subscriber.onMessage(redisMessage, null))
                .isInstanceOf(IllegalStateException.class)
                .hasCauseInstanceOf(java.io.IOException.class);
        verifyNoInteractions(eventPublisher);
    }
}
