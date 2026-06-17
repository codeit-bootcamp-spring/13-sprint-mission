package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.User;

import java.util.UUID;

public record UserFindResponse (
        UUID userId,
        String name,
        String email,
        UUID profileId,
        boolean userOnline
) {
    public static UserFindResponse from(User user, boolean userOnline) {
        return new UserFindResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProfileId(),
                userOnline
        );
    }
}
