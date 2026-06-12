package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;

import java.time.Instant;
import java.util.UUID;

public record UserUpdateResponse(
        UUID userId,
        Instant createdAt,
        Instant updatedAt,
        String name,
        String email,
        UUID profileId
) {
    public static UserUpdateResponse from(User user) {
        return new UserUpdateResponse(
                user.getId(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getName(),
                user.getEmail(),
                user.getProfileId()
        );
    }
}
