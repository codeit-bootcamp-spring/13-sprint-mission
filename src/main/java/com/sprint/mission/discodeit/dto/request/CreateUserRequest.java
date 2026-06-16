package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.entity.User;

public record CreateUserRequest (
        String username,
        String email,
        String password
) {
    public User toEntity() {
        return new User(
                username,
                email,
                password
        );
    }
}
