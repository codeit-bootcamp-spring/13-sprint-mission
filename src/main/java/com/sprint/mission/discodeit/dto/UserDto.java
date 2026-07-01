package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.*;

import java.time.*;
import java.util.*;

public record UserDto(UUID id,
                      Instant createdAt,
                      Instant updatedAt,
                      String username,
                      String email,
                      UUID profileId,
                      Boolean online) {

    public static UserDto from(User user, UserStatus userStatus, BinaryContent profile) {
        return new UserDto(
                user.getId(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getUserName(),
                user.getEmail(),
                profile == null ? null : profile.getId(),
                userStatus != null && userStatus.isOnline()
        );
    }
}
