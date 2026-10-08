package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationRequiredEventListener {

    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @TransactionalEventListener
    public void on(MessageCreatedEvent event) {
        Message message = messageRepository.findById(event.messageId())
                .orElseThrow(() -> new MessageNotFoundException(event.messageId()));

        UUID authorId = message.getAuthor().getId();

        // 해당 메시지 보낸 사람 외의 알림 허용한 채널 이용자들한테만 알림 생성하도록
        List<Notification> notifications = readStatusRepository
                .findAllByChannelIdAndNotificationEnabledTrue(message.getChannel().getId()).stream()
                .filter(readStatus -> !readStatus.getUser().getId().equals(authorId))
                .map(readStatus -> new Notification(
                        readStatus.getUser(),
                        message.getAuthor().getUsername()
                                + " (#"
                                + message.getChannel().getName()
                                + ")",
                        message.getContent()
                ))
                .toList();

        notificationRepository.saveAll(notifications);
    }

    @TransactionalEventListener
    public void on(RoleUpdatedEvent event) {
        User receiver = userRepository.findById(event.userId())
                .orElseThrow(() -> new UserNotFoundException(event.userId()));

        //권한 변경 알림 생성하도록
        Notification notification = new Notification(
                receiver,
                "권한이 변경되었습니다.",
                event.oldRole() + " -> " + event.newRole()
        );

        notificationRepository.save(notification);
    }
}
