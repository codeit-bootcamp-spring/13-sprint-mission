package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserRole;
import com.sprint.mission.discodeit.entity.enums.ChannelType;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class NotificationRequiredEventListenerTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ReadStatusRepository readStatusRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationRequiredEventListener listener;

    @Test
    void on_메시지_작성자를_제외하고_알림을_생성한다() {
        UUID messageId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        Channel channel = new Channel(
                "general",
                "일반 채널",
                ChannelType.PUBLIC
        );

        ReflectionTestUtils.setField(
                channel,
                "id",
                channelId
        );

        User sender = new User(
                "sender",
                "sender@example.com",
                "encoded-password"
        );

        ReflectionTestUtils.setField(
                sender,
                "id",
                senderId
        );

        User receiver = new User(
                "receiver",
                "receiver@example.com",
                "encoded-password"
        );

        ReflectionTestUtils.setField(
                receiver,
                "id",
                receiverId
        );

        Message message = new Message(
                "안녕하세요.",
                channel,
                sender
        );

        ReflectionTestUtils.setField(
                message,
                "id",
                messageId
        );

        ReadStatus senderReadStatus =
                new ReadStatus(channel, sender);
        ReadStatus receiverReadStatus =
                new ReadStatus(channel, receiver);

        given(messageRepository.findById(messageId))
                .willReturn(Optional.of(message));

        given(
                readStatusRepository
                        .findAllByChannelIdAndNotificationEnabledTrue(
                                channelId
                        )
        ).willReturn(
                List.of(
                        senderReadStatus,
                        receiverReadStatus
                )
        );

        listener.on(new MessageCreatedEvent(messageId));

        then(notificationService)
                .should()
                .create(
                        receiverId,
                        "sender (#general)",
                        "안녕하세요."
                );

        then(notificationService)
                .should(never())
                .create(
                        eq(senderId),
                        anyString(),
                        anyString()
                );
    }

    @Test
    void on_권한이_변경된_사용자에게_알림을_생성한다() {
        UUID userId = UUID.randomUUID();

        RoleUpdatedEvent event = new RoleUpdatedEvent(
                userId,
                UserRole.USER,
                UserRole.CHANNEL_MANAGER
        );

        listener.on(event);

        then(notificationService)
                .should()
                .create(
                        userId,
                        "권한이 변경되었습니다.",
                        "USER -> CHANNEL_MANAGER"
                );
    }

    @Test
    void on_메시지가_없으면_실패한다() {
        UUID messageId = UUID.randomUUID();

        given(messageRepository.findById(messageId))
                .willReturn(Optional.empty());

        assertThatThrownBy(
                () -> listener.on(
                        new MessageCreatedEvent(messageId)
                )
        )
                .isInstanceOf(MessageNotFoundException.class)
                .hasMessage("메시지를 찾을 수 없습니다.");

        then(readStatusRepository).shouldHaveNoInteractions();
        then(notificationService).shouldHaveNoInteractions();
    }
}