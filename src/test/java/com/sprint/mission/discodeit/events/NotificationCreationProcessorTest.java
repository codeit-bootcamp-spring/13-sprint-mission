package com.sprint.mission.discodeit.events;

import com.sprint.mission.discodeit.dto.command.notification.NotificationCreateCommand;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.service.NotificationService;
import com.sprint.mission.discodeit.service.basic.ReadStatusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static com.sprint.mission.discodeit.support.NotificationTestFixtures.messageCreatedEvent;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * 실제 알림 생성 처리기가 수신자 조회 결과를 알림 생성 요청으로 변환하는지 확인한다.
 * 조회·저장은 Mock 서비스로 분리하고 채팅 DTO를 포함한 이벤트는 실제 값으로 전달한다.
 */
@ExtendWith(MockitoExtension.class)
class NotificationCreationProcessorTest {
    @Mock ReadStatusService readStatusService;
    @Mock NotificationService notificationService;
    @InjectMocks NotificationCreationProcessor listener;

    @Test
    void message_createsNotificationsForRecipientsReturnedByReadStatusService() {
        MessageCreatedEvent event = messageCreatedEvent(UUID.randomUUID(), UUID.randomUUID(), "general", "sender", "hello");
        List<UUID> recipients = List.of(UUID.randomUUID(), UUID.randomUUID());
        given(readStatusService.findNotificationRecipientIds(event.channelId(), event.authorId())).willReturn(recipients);

        listener.handleMessageCreated(event);

        verify(notificationService).createAll(new NotificationCreateCommand(recipients, "sender (#general)", "hello"));
    }

    @Test
    void message_doesNotCreateNotificationsWhenNobodySubscribed() {
        MessageCreatedEvent event = messageCreatedEvent(UUID.randomUUID(), UUID.randomUUID(), "general", "sender", "hello");
        given(readStatusService.findNotificationRecipientIds(event.channelId(), event.authorId())).willReturn(List.of());
        listener.handleMessageCreated(event);
        verifyNoInteractions(notificationService);
    }

    @Test
    void role_createsNotificationOnlyForAffectedUser() {
        UUID userId = UUID.randomUUID();
        listener.handleRoleUpdated(new RoleUpdatedEvent(userId, Role.USER, Role.CHANNEL_MANAGER));
        verify(notificationService).createAll(new NotificationCreateCommand(List.of(userId), "권한이 변경되었습니다.", "USER -> CHANNEL_MANAGER"));
        verifyNoInteractions(readStatusService);
    }

    @Test
    void message_propagatesRecipientLookupFailureWithoutCreatingNotifications() {
        MessageCreatedEvent event = messageCreatedEvent(UUID.randomUUID(), UUID.randomUUID(), "general", "sender", "hello");
        RuntimeException failure = new IllegalStateException("수신자 조회 실패");
        given(readStatusService.findNotificationRecipientIds(event.channelId(), event.authorId())).willThrow(failure);

        assertThatThrownBy(() -> listener.handleMessageCreated(event)).isSameAs(failure);
        verifyNoInteractions(notificationService);
    }

    @Test
    void message_propagatesNotificationStorageFailure() {
        MessageCreatedEvent event = messageCreatedEvent(UUID.randomUUID(), UUID.randomUUID(), "general", "sender", "hello");
        List<UUID> recipients = List.of(UUID.randomUUID());
        given(readStatusService.findNotificationRecipientIds(event.channelId(), event.authorId())).willReturn(recipients);
        RuntimeException failure = new IllegalStateException("메시지 알림 저장 실패");
        NotificationCreateCommand command = new NotificationCreateCommand(recipients, event);
        doThrow(failure).when(notificationService).createAll(command);

        assertThatThrownBy(() -> listener.handleMessageCreated(event)).isSameAs(failure);
        verify(notificationService).createAll(command);
    }

    @Test
    void role_propagatesNotificationStorageFailure() {
        RoleUpdatedEvent event = new RoleUpdatedEvent(UUID.randomUUID(), Role.USER, Role.CHANNEL_MANAGER);
        RuntimeException failure = new IllegalStateException("권한 변경 알림 저장 실패");
        NotificationCreateCommand command = new NotificationCreateCommand(event.userId(), event);
        doThrow(failure).when(notificationService).createAll(command);

        assertThatThrownBy(() -> listener.handleRoleUpdated(event)).isSameAs(failure);
        verify(notificationService).createAll(command);
    }
}
