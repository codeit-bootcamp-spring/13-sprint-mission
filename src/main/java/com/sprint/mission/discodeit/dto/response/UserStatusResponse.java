package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.*;

import java.util.*;

public record UserStatusResponse(
        UUID userId,
        boolean online
) {
    public static UserStatusResponse from(UserStatus userStatus) {
        return new UserStatusResponse(
                userStatus.getUser().getId(),
                userStatus.isOnline()
        );
    }

}
