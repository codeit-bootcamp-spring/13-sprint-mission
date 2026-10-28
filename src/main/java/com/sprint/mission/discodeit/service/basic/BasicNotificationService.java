package com.sprint.mission.discodeit.service.basic;


import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
import com.sprint.mission.discodeit.mapper.EntityMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicNotificationService implements NotificationService {

    private final ReadStatusRepository readStatusRepository;
    private final NotificationRepository notificationRepository;
    private final EntityMapper entityMapper;


    @Transactional
    @Override
    public NotificationDto createNotifies(UUID receiver, String title, String content) {
        Notification noty = notificationRepository.save(new Notification(receiver, title, content));
        return entityMapper.toDto(noty);
    }

    private Notification getNotificationFromUser(MessageDto messageDto, User user){
        if (user.getId().equals(messageDto.author().id()))
            // 작성자 == 유저 라면 알림 생략
            return null;
        else{
            log.debug("[MessageEvent] - 아이디 유저 체크 - 작성자 : {}, 알림 대상 : {}", messageDto.author().id(),user.getId());
            return new Notification(
                    user.getId(),   // 이 친구가 대상자 인데, 트랜젝션에 의해 값이 사라진다?
                    user.getUsername(),
                    messageDto.content()
            );
        }
    }

    //    @Cacheable(DiscodeItCacheConfig.NOTIFICATION_CACHE)
    @Transactional(readOnly = true)
    @Override
    public List<NotificationDto> getNotifies(Authentication authentication){



        return notificationRepository.findAllByReceiverId(
                userIdFromAuth(authentication)
                )
                .stream().map(entityMapper::toDto).toList();
    }

    // 알림을 삭제하면, 현재 알림 목록 캐시를 비운다.
//    @CacheEvict(cacheNames = DiscodeItCacheConfig.NOTIFICATION_CACHE, allEntries = true)
    @Transactional
    @Override
    public void deleteNotify(UUID id, Authentication authentication){

        Notification noty = notificationRepository.findById(id)
                .orElseThrow(() -> new DiscodeitException(ExceptionCode.RESOURCE_NOT_FOUNDED));

        log.debug("수신자 : {} , 현재 사용자 : {}",noty.getReceiverId(), userIdFromAuth(authentication));

        // !(요청자 == 메세지 수신자) => 403 error
        if (!noty.getReceiverId().equals(userIdFromAuth(authentication)))
            throw new DiscodeitException(ExceptionCode.INVALID_AUTH_ERROR);

        notificationRepository.deleteById(id);
    }


    private UUID userIdFromAuth(Authentication auth){
        // todo - auth 에서 유저 추출. 매서드 에러 체크 필요
        DiscodeitUserDetails userDetails = (DiscodeitUserDetails) auth.getPrincipal();

        return userDetails.getUserDto().id();
    }


}
