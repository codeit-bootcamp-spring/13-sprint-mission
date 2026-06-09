package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.*;

import java.time.*;
import java.util.*;

public record UserResponse(
        UUID id,
        String userName,
        String email,
        boolean onlineStatus,
        Instant lastOnlineAt) {

    public static UserResponse from(User user, UserStatus userStatus) {
        return new UserResponse(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                userStatus.isOnline(),
                userStatus.getLastOnlineAt()
        );
    }
}
