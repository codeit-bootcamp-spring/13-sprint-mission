package com.sprint.mission.discodeit.event.role;


import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RoleEventListener {

    private final NotificationRepository notificationRepository;

    @TransactionalEventListener
    public void on(RoleUpdateEvent event){
        notificationRepository.save(new Notification(
                event.id(),
                "권한 변경 알림",
                "권한이 변경되었습니다. - " + event.role().toString()
        ));
    }

}
