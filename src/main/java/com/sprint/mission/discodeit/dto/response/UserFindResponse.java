package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.User;

import java.time.Instant;
import java.util.UUID;

public record UserFindResponse (
        UUID userId,
        Instant createdAt,
        Instant updatedAt,
        String name,
        String email,
        UUID profileId,
        boolean userOnline
) {
    public static UserFindResponse from(User user, boolean userOnline) {
        return new UserFindResponse(
                user.getId(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getName(),
                user.getEmail(),
                user.getProfileId(),
                userOnline
        );
    }
}
