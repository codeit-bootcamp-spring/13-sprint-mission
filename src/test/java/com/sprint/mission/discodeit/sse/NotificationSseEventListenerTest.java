package com.sprint.mission.discodeit.sse;

import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.event.NotificationCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class NotificationSseEventListenerTest {

    @Test
    @DisplayName("알림 이벤트를 수신하면 해당 사용자에게 SSE 알림을 전송한다")
    void handleNotificationCreatedEvent_success() {
        // given
        SseService sseService = mock(SseService.class);

        NotificationSseEventListener listener =
                new NotificationSseEventListener(sseService);

        UUID receiverId = UUID.randomUUID();
        NotificationDto notification = mock(NotificationDto.class);

        NotificationCreatedEvent event =
                new NotificationCreatedEvent(receiverId, notification);

        // when
        listener.handleNotificationCreated(event);

        // then
        then(sseService).should()
                .send(
                        Set.of(receiverId),
                        "notifications.created",
                        notification
                );
    }
}
