package com.sprint.mission.discodeit.event.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@ExtendWith(MockitoExtension.class)
class KafkaProduceRequiredEventListenerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private KafkaProduceRequiredEventListener listener;

    @BeforeEach
    void setUp() {
        // KafkaTemplate.send()는 CompletableFuture를 반환하므로
        // 정상 발행 요청 테스트에서 완료된 Future를 반환하도록 설정
    }

    @Test
    @DisplayName("메시지 생성 이벤트를 지정된 Kafka 토픽과 메시지 ID를 키로 발행")
    void message_created_event_success() throws Exception {
        // given
        UUID messageId = UUID.randomUUID();
        MessageCreatedEvent event = new MessageCreatedEvent(messageId);

        String payload = "{\"messageId\":\"" + messageId + "\"}";

        given(objectMapper.writeValueAsString(event))
                .willReturn(payload);

        given(kafkaTemplate.send(
                "discodeit.MessageCreatedEvent",
                messageId.toString(),
                payload
        )).willReturn(CompletableFuture.completedFuture(null));

        // when
        listener.on(event);

        // then
        then(kafkaTemplate).should().send(
                "discodeit.MessageCreatedEvent",
                messageId.toString(),
                payload
        );
    }

    @Test
    @DisplayName("권한 변경 이벤트를 지정된 Kafka 토픽과 사용자 ID를 키로 발행")
    void role_updated_event_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        RoleUpdatedEvent event = new RoleUpdatedEvent(
                userId,
                Role.USER,
                Role.CHANNEL_MANAGER
        );

        String payload = "{\"userId\":\"" + userId + "\"}";

        given(objectMapper.writeValueAsString(event))
                .willReturn(payload);

        given(kafkaTemplate.send(
                "discodeit.RoleUpdatedEvent",
                userId.toString(),
                payload
        )).willReturn(CompletableFuture.completedFuture(null));

        // when
        listener.on(event);

        // then
        then(kafkaTemplate).should().send(
                "discodeit.RoleUpdatedEvent",
                userId.toString(),
                payload
        );
    }

    @Test
    @DisplayName("S3 업로드 실패 이벤트를 지정된 Kafka 토픽과 파일 ID를 키로 발행")
    void s3_upload_failed_event_success() throws Exception {
        // given
        UUID binaryContentId = UUID.randomUUID();
        S3UploadFailedEvent event = new S3UploadFailedEvent(
                "S3 Upload",
                "request-123",
                binaryContentId,
                "S3 업로드 실패"
        );

        String payload = "{\"binaryContentId\":\"" + binaryContentId + "\"}";

        given(objectMapper.writeValueAsString(event))
                .willReturn(payload);

        given(kafkaTemplate.send(
                "discodeit.S3UploadFailedEvent",
                binaryContentId.toString(),
                payload
        )).willReturn(CompletableFuture.completedFuture(null));

        // when
        listener.on(event);

        // then
        then(kafkaTemplate).should().send(
                "discodeit.S3UploadFailedEvent",
                binaryContentId.toString(),
                payload
        );
    }

    @Test
    @DisplayName("메시지 생성 이벤트 직렬화 실패 시 예외 발생")
    void message_created_event_serialization_failure() throws Exception {
        // given
        MessageCreatedEvent event = new MessageCreatedEvent(UUID.randomUUID());

        given(objectMapper.writeValueAsString(event))
                .willThrow(new JsonProcessingException("직렬화 실패") {});

        // when & then
        assertThatThrownBy(() -> listener.on(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("MessageCreatedEvent 직렬화에 실패했습니다.");

        then(kafkaTemplate).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("권한 변경 이벤트 직렬화 실패 시 예외 발생")
    void role_updated_event_serialization_failure() throws Exception {
        // given
        RoleUpdatedEvent event = new RoleUpdatedEvent(
                UUID.randomUUID(),
                Role.USER,
                Role.CHANNEL_MANAGER
        );

        given(objectMapper.writeValueAsString(event))
                .willThrow(new JsonProcessingException("직렬화 실패") {});

        // when & then
        assertThatThrownBy(() -> listener.on(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("RoleUpdatedEvent 직렬화에 실패했습니다.");

        then(kafkaTemplate).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("S3 업로드 실패 이벤트 직렬화 실패 시 예외 발생")
    void s3_upload_failed_event_serialization_failure() throws Exception {
        // given
        S3UploadFailedEvent event = new S3UploadFailedEvent(
                "S3 Upload",
                "request-123",
                UUID.randomUUID(),
                "S3 업로드 실패"
        );

        given(objectMapper.writeValueAsString(event))
                .willThrow(new JsonProcessingException("직렬화 실패") {});

        // when & then
        assertThatThrownBy(() -> listener.on(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("S3UploadFailedEvent 직렬화에 실패했습니다.");

        then(kafkaTemplate).shouldHaveNoInteractions();
    }
}