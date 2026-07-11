package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.entity.UserStatus;
import org.springframework.stereotype.Component;

@Component
public class UserStatusMapper {

    public UserStatusResponse toDto(UserStatus userStatus) {
        if (userStatus == null) {
            return null;
        }

        return new UserStatusResponse(
                userStatus.getId(),
                userStatus.getUser().getId(),
                userStatus.getLastSeenAt(),
                userStatus.isOnline()
        );
    }
}