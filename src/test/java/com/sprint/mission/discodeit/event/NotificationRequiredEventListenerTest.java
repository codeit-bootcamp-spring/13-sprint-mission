package com.sprint.mission.discodeit.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationRequiredEventListenerTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ReadStatusRepository readStatusRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationRequiredEventListener listener;

    @Test
    void shouldCreateNotificationsForEnabledUsersExceptMessageAuthor() {
        // given
        UUID messageId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        User author = mock(User.class);
        User receiver = mock(User.class);
        Channel channel = mock(Channel.class);
        Message message = mock(Message.class);

        ReadStatus authorReadStatus = mock(ReadStatus.class);
        ReadStatus receiverReadStatus = mock(ReadStatus.class);

        given(messageRepository.findById(messageId))
                .willReturn(Optional.of(message));

        given(message.getAuthor())
                .willReturn(author);

        given(message.getChannel())
                .willReturn(channel);

        given(message.getContent())
                .willReturn("테스트 메시지");

        given(author.getId())
                .willReturn(authorId);

        given(author.getUsername())
                .willReturn("작성자");

        given(channel.getId())
                .willReturn(channelId);

        given(channel.getName())
                .willReturn("테스트 채널");

        given(authorReadStatus.getUser())
                .willReturn(author);

        given(receiverReadStatus.getUser())
                .willReturn(receiver);

        given(receiver.getId())
                .willReturn(receiverId);

        given(readStatusRepository
                .findAllByChannelIdAndNotificationEnabledTrue(channelId))
                .willReturn(List.of(
                        authorReadStatus,
                        receiverReadStatus
                ));

        // when
        listener.on(
                new MessageCreatedEvent(messageId)
        );

        // then
        ArgumentCaptor<List<Notification>> captor =
                ArgumentCaptor.forClass(List.class);

        then(notificationRepository).should()
                .saveAll(captor.capture());

        List<Notification> notifications =
                captor.getValue();

        assertThat(notifications)
                .hasSize(1);

        Notification notification =
                notifications.get(0);

        assertThat(notification.getReceiver())
                .isEqualTo(receiver);

        assertThat(notification.getTitle())
                .isEqualTo("작성자 (#테스트 채널)");

        assertThat(notification.getContent())
                .isEqualTo("테스트 메시지");
    }

    @Test
    void shouldCreateNotificationForUserWhenRoleIsUpdated() {
        // given
        UUID userId = UUID.randomUUID();

        User user = mock(User.class);

        given(userRepository.findById(userId))
                .willReturn(Optional.of(user));

        RoleUpdatedEvent event =
                new RoleUpdatedEvent(
                        userId,
                        Role.USER,
                        Role.CHANNEL_MANAGER
                );

        // when
        listener.on(event);

        // then
        ArgumentCaptor<Notification> captor =
                ArgumentCaptor.forClass(Notification.class);

        then(notificationRepository).should()
                .save(captor.capture());

        Notification notification =
                captor.getValue();

        assertThat(notification.getReceiver())
                .isEqualTo(user);

        assertThat(notification.getTitle())
                .isEqualTo("권한이 변경되었습니다.");

        assertThat(notification.getContent())
                .isEqualTo("USER -> CHANNEL_MANAGER");
    }
}