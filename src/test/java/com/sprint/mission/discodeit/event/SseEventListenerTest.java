package com.sprint.mission.discodeit.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.event.kafka.SseKafkaEvent;
import com.sprint.mission.discodeit.service.UserService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class SseEventListenerTest {

    private static final String TOPIC = "discodeit.sse.events";

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private UserService userService;

    private ObjectMapper objectMapper;
    private SseEventListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        listener = new SseEventListener(
                kafkaTemplate,
                objectMapper,
                userService
        );
    }

    @Test
    @DisplayName("알림 생성 이벤트를 특정 사용자에게 발행")
    void notification_created_success() throws Exception {
        // given
        UUID receiverId = UUID.randomUUID();

        NotificationCreatedEvent event =
                mock(NotificationCreatedEvent.class);

        given(event.receiverId())
                .willReturn(receiverId);

        // when
        listener.on(event);

        // then
        assertPublishedEvent(
                "notifications.created",
                List.of(receiverId)
        );
    }

    @Test
    @DisplayName("바이너리 콘텐츠 수정 이벤트를 전체 사용자에게 발행")
    void binary_content_updated_success() throws Exception {
        // given
        BinaryContentUpdatedEvent event =
                mock(BinaryContentUpdatedEvent.class);

        // when
        listener.on(event);

        // then
        assertPublishedEvent(
                "binaryContents.updated",
                null
        );
    }

    @Test
    @DisplayName("채널 생성 이벤트를 지정된 사용자들에게 발행")
    void channel_created_success() throws Exception {
        // given
        List<UUID> receiverIds = List.of(
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        ChannelCreatedEvent event =
                mock(ChannelCreatedEvent.class);

        given(event.receiverIds())
                .willReturn(receiverIds);

        // when
        listener.on(event);

        // then
        assertPublishedEvent(
                "channels.created",
                receiverIds
        );
    }

    @Test
    @DisplayName("채널 수정 이벤트를 전체 사용자에게 발행")
    void channel_updated_success() throws Exception {
        // given
        ChannelUpdatedEvent event =
                mock(ChannelUpdatedEvent.class);

        // when
        listener.on(event);

        // then
        assertPublishedEvent(
                "channels.updated",
                null
        );
    }

    @Test
    @DisplayName("채널 삭제 이벤트를 지정된 사용자들에게 발행")
    void channel_deleted_success() throws Exception {
        // given
        List<UUID> receiverIds = List.of(
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        ChannelDeletedEvent event =
                mock(ChannelDeletedEvent.class);

        given(event.receiverIds())
                .willReturn(receiverIds);

        // when
        listener.on(event);

        // then
        assertPublishedEvent(
                "channels.deleted",
                receiverIds
        );
    }

    @Test
    @DisplayName("사용자 생성 이벤트를 Kafka로 발행하고 DTO 데이터를 포함")
    void user_created_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        UserDto userDto = new UserDto(
                userId,
                "testUser",
                "test@test.com",
                null,
                false,
                Role.USER
        );

        UserCreatedEvent event =
                mock(UserCreatedEvent.class);

        given(event.userDto())
                .willReturn(userDto);

        // when
        listener.on(event);

        // then
        JsonNode payload = capturePublishedPayload();

        assertThat(payload.path("eventName").asText())
                .isEqualTo("users.created");

        assertThat(payload.path("receiverIds").isNull())
                .isTrue();

        assertThat(payload.path("data").path("id").asText())
                .isEqualTo(userId.toString());

        assertThat(payload.path("data").path("username").asText())
                .isEqualTo("testUser");
    }

    @Test
    @DisplayName("사용자 수정 이벤트를 전체 사용자에게 발행")
    void user_updated_success() throws Exception {
        // given
        UserUpdatedEvent event =
                mock(UserUpdatedEvent.class);

        // when
        listener.on(event);

        // then
        assertPublishedEvent(
                "users.updated",
                null
        );
    }

    @Test
    @DisplayName("사용자 삭제 이벤트를 전체 사용자에게 발행")
    void user_deleted_success() throws Exception {
        // given
        UserDeletedEvent event =
                mock(UserDeletedEvent.class);

        // when
        listener.on(event);

        // then
        assertPublishedEvent(
                "users.deleted",
                null
        );
    }

    @Test
    @DisplayName("로그인 및 로그아웃 이벤트에서 사용자 정보를 조회하고 수정 이벤트 발행")
    void user_login_logout_success() throws Exception {
        // given
        UUID userId = UUID.randomUUID();

        UserLogInOutEvent event =
                mock(UserLogInOutEvent.class);

        UserDto userDto = new UserDto(
                userId,
                "testUser",
                "test@test.com",
                null,
                true,
                Role.USER
        );

        given(event.userId())
                .willReturn(userId);

        given(userService.getUser(userId))
                .willReturn(userDto);

        // when
        listener.on(event);

        // then
        then(userService).should()
                .getUser(userId);

        JsonNode payload = capturePublishedPayload();

        assertThat(payload.path("eventName").asText())
                .isEqualTo("users.updated");

        assertThat(payload.path("receiverIds").isNull())
                .isTrue();

        assertThat(payload.path("data").path("id").asText())
                .isEqualTo(userId.toString());

        assertThat(payload.path("data").path("online").asBoolean())
                .isTrue();
    }

    @Test
    @DisplayName("Kafka 이벤트 직렬화 실패 시 예외 발생 및 발행 방지")
    void serialization_failure() throws Exception {
        // given
        ObjectMapper mockedObjectMapper = mock(ObjectMapper.class);

        SseEventListener failingListener = new SseEventListener(
                kafkaTemplate,
                mockedObjectMapper,
                userService
        );

        UserUpdatedEvent event =
                mock(UserUpdatedEvent.class);

        given(mockedObjectMapper.writeValueAsString(
                any(SseKafkaEvent.class)
        )).willThrow(
                new JsonProcessingException("직렬화 실패") {}
        );

        // when & then
        assertThatThrownBy(
                () -> failingListener.on(event)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "SSE Kafka 이벤트 직렬화에 실패했습니다."
                )
                .hasCauseInstanceOf(JsonProcessingException.class);

        then(kafkaTemplate).shouldHaveNoInteractions();
    }

    private void assertPublishedEvent(
            String expectedEventName,
            List<UUID> expectedReceiverIds
    ) throws Exception {
        JsonNode payload = capturePublishedPayload();

        assertThat(payload.path("eventName").asText())
                .isEqualTo(expectedEventName);

        JsonNode receiverIds = payload.path("receiverIds");

        if (expectedReceiverIds == null) {
            assertThat(receiverIds.isNull())
                    .isTrue();
        } else {
            assertThat(receiverIds.isArray())
                    .isTrue();

            assertThat(receiverIds.size())
                    .isEqualTo(expectedReceiverIds.size());

            for (int i = 0; i < expectedReceiverIds.size(); i++) {
                assertThat(receiverIds.get(i).asText())
                        .isEqualTo(expectedReceiverIds.get(i).toString());
            }
        }
    }

    private JsonNode capturePublishedPayload() throws Exception {
        ArgumentCaptor<String> payloadCaptor =
                ArgumentCaptor.forClass(String.class);

        then(kafkaTemplate).should()
                .send(eq(TOPIC), payloadCaptor.capture());

        return objectMapper.readTree(payloadCaptor.getValue());
    }
}