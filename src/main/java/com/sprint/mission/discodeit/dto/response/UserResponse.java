package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.UserRole;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        Boolean online,
        BinaryContentResponse profile,
        UserRole role
) {

    public UserResponse(
            UUID id,
            String username,
            String email,
            Boolean online,
            BinaryContentResponse profile
    ) {
        this(
                id,
                username,
                email,
                online,
                profile,
                UserRole.USER
        );
    }
}