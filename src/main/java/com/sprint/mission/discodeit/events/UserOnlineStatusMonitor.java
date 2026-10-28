package com.sprint.mission.discodeit.events;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.repository.UserOnlineTrackingRepository;
import com.sprint.mission.discodeit.security.JwtRegistry;
import com.sprint.mission.discodeit.service.basic.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 추적 중인 사용자의 액세스 토큰 만료를 감지하여 사용자 갱신 이벤트를 발행한다.
 * 리프레시 토큰이 남아 있어도 유효한 액세스 토큰이 없으면 온라인 표시를 갱신한다.
 * 상태 재확인과 추적 제거는 JwtRegistry에 맡겨 로그인·갱신과의 경쟁을 처리한다.
 */
@Component
@RequiredArgsConstructor
public class UserOnlineStatusMonitor {

    private final UserReader userReader;
    private final ApplicationEventPublisher eventPublisher;
    private final JwtRegistry jwtRegistry;
    private final UserOnlineTrackingRepository userOnlineTrackingRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleUserEvent(UserEvent event){
        UUID userId = event.user().id();

        if(event.isDeleteEvent()){
            userOnlineTrackingRepository.remove(userId);
            return;
        }

        if (event.user().offline()){
            jwtRegistry.tryRemoveOfflineTracking(userId);
        }
    }

    /**
     * 추적 ID를 한 번 조회하고 사용자 DTO를 일괄 조회해 만료 상태를 확인한다.
     * 오프라인 상태를 재확인하면서 추적 항목 제거에 성공한 호출만 갱신 이벤트를 발행한다.
     * 제거 이후 발행 실패를 재시도하는 구조는 아니므로 정확히 한 번 전달을 보장하지는 않는다.
     */
    @Scheduled(fixedDelay = 60_000)
    public void checkOnlineStatus(){
        Set<UUID> targetUserIds = userOnlineTrackingRepository.findAll();

        if (targetUserIds.isEmpty()){
            return;
        }

        Map<UUID, UserDto> users = userReader.findUserDtosByIds(targetUserIds);

        targetUserIds.forEach(userId -> {
            UserDto user = users.get(userId);
            if (user == null){
                userOnlineTrackingRepository.remove(userId);
                return;
            }

            if (user.offline() &&  jwtRegistry.tryRemoveOfflineTracking(userId)){
                eventPublisher.publishEvent(UserEvent.updateEvent(user));
            }
        });
    }
}
