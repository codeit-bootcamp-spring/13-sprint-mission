package com.sprint.mission.discodeit.event.system;

import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.role.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;


@Component
@RequiredArgsConstructor
public class SystemEventListener {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * 시스템 작업 오류 메세지를 관리자 원한을 가진 사용자에게 알림 발송.
     * @param event - 1. 작업 내용, 2. 리퀘스트 아이디, 3. 발생한 에러 메세지
     */
    @Async("noty")
    @TransactionalEventListener
    public void on(SystemNotifyEvent event){
        List<User> admins = userRepository.findByRole(Role.ADMIN);

        admins.forEach(
                user -> notificationRepository.save(new Notification(
                            user.getId(),
                            event.job() + "작업 실패",
                            message(event.job(), event.requestId(), event.exceptionMessage())
                ))

        );
    }

    private String message(String job, String requestId, String exceptionMessage){
        StringBuilder message = new StringBuilder();
        message.append("작업 이름: ").append(job);
        message.append("요청 ID: ").append(requestId);
        message.append("에러 메세지: ").append(exceptionMessage);
        return message.toString();
    }
}
