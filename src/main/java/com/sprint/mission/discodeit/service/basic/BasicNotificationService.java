package com.sprint.mission.discodeit.service.basic;


import com.sprint.mission.discodeit.config.cache.CacheConfig;
import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
import com.sprint.mission.discodeit.mapper.EntityMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicNotificationService implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EntityMapper entityMapper;

    @Cacheable(CacheConfig.NOTIFICATION_CACHE)
    @Transactional(readOnly = true)
    @Override
    public List<NotificationDto> getNotifies(Authentication authentication){



        return notificationRepository.findAllByReceiverId(
                userIdFromAuth(authentication)
                )
                .stream().map(entityMapper::toDto).toList();
    }

    // 알림을 삭제하면, 현재 알림 목록 캐시를 비운다.
    @CacheEvict(cacheNames = CacheConfig.NOTIFICATION_CACHE, allEntries = true)
    @Transactional
    @Override
    public void deleteNotify(UUID id, Authentication authentication){

        Notification noty = notificationRepository.findById(id)
                .orElseThrow(() -> new DiscodeitException(ExceptionCode.RESOURCE_NOT_FOUNDED));

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
