package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationRequiredEventListener {

    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(MessageCreatedEvent event) {
        Message message = messageRepository
                .findById(event.messageId())
                .orElseThrow(
                        () -> new MessageNotFoundException(
                                event.messageId()
                        )
                );

        UUID senderId = message.getAuthor().getId();
        UUID channelId = message.getChannel().getId();

        List<ReadStatus> readStatuses =
                readStatusRepository
                        .findAllByChannelIdAndNotificationEnabledTrue(
                                channelId
                        );

        String title = "%s (#%s)".formatted(
                message.getAuthor().getUsername(),
                message.getChannel().getName()
        );

        for (ReadStatus readStatus : readStatuses) {
            UUID receiverId = readStatus.getUser().getId();

            if (receiverId.equals(senderId)) {
                continue;
            }

            notificationService.create(
                    receiverId,
                    title,
                    message.getContent()
            );
        }
    }

    @Async
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(RoleUpdatedEvent event) {
        String content = "%s -> %s".formatted(
                event.previousRole(),
                event.updatedRole()
        );

        notificationService.create(
                event.userId(),
                "권한이 변경되었습니다.",
                content
        );
    }
}