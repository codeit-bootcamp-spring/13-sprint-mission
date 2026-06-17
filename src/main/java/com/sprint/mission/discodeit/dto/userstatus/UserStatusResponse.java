package com.sprint.mission.discodeit.dto.userstatus;

import com.sprint.mission.discodeit.entity.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserStatusResponse(
        UUID userId,
        Instant lastOnlineAt
) {
    public static UserStatusResponse from(UserStatus userStatus) {
        return new UserStatusResponse(userStatus.getUserId(), userStatus.getLastOnlineAt());
    }
}
