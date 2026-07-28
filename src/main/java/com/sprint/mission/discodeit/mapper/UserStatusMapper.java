package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.UserStatusResponse;
import com.sprint.mission.discodeit.entity.UserStatus;
import org.springframework.stereotype.Component;

@Component
public class UserStatusMapper {

    public UserStatusResponse toDto(UserStatus entity) {
        if (entity == null) {
            return null;
        }

        return new UserStatusResponse(
                entity.getId(),
                entity.getUser().getId(),
                entity.getLastActiveAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.isOnline()
        );
    }
}