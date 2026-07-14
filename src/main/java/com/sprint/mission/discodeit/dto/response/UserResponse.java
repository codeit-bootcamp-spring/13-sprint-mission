package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.*;

import java.time.*;
import java.util.*;

public record UserResponse(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        String username,
        String email,
        UUID profileId,
        Boolean online) {

    public static UserResponse from(User user, UserStatus userStatus, BinaryContent profile) {
        return new UserResponse(
                user.getId(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getUsername(),
                user.getEmail(),
                profile == null ? null : profile.getId(),
                userStatus != null && userStatus.isOnline()
        );
    }
}