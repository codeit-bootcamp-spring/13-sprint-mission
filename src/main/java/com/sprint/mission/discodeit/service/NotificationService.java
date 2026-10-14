package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.response.NotificationDto;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    List<NotificationDto> getNotifies(Authentication authentication);
    void deleteNotify(UUID id, Authentication authentication);
}
