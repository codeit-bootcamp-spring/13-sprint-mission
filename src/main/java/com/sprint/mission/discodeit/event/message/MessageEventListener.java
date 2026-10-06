package com.sprint.mission.discodeit.event.message;


import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class MessageEventListener {

    private final ReadStatusRepository readStatusRepository;
    private final NotificationRepository notificationRepository;


    /**
     * 메세지를 생성할 때, 해당 채널을 구독한 구독자에게 알림을 보냄.
     * 단, 메세지를 생성한 사용자는 제외.
     * @param event 메세지 생성 이벤트
     */
    @TransactionalEventListener
    public void on(MessageCreateEvent event){
        List<ReadStatus> readStatuses = readStatusRepository.findAllByChannelIdWithStatusOn(event.channelId(),true);

        List<Notification> notifications = readStatuses.stream()
                .map(readStatus -> getNotificationFromUser(event,readStatus.getUser()))
                .filter(Objects::nonNull)
                .toList();

        notificationRepository.saveAll(notifications);
    }

    private Notification getNotificationFromUser(MessageCreateEvent event, User user){
        if (user.getId().equals(event.userId()))
            return null;
        else
            return new Notification(
                    user.getId(),
                    user.getUsername(),
                    event.content()
            );
    }

}
