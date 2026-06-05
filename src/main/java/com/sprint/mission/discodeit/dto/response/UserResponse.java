package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.User;

import java.util.UUID;

public record UserResponse (
    UUID id,
    String username,
    String email,
    UUID profileId,
    boolean isOnline
){


    public static UserResponse from(User user, boolean isOnline) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(), user.getProfileId(), isOnline);
    }
}
