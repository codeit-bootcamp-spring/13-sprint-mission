package com.sprint.mission.discodeit.dto.command;

import com.sprint.mission.discodeit.dto.request.*;

public record CreateUserCommand(
        String username,
        String email,
        String password
) {
    public static CreateUserCommand from(CreateUserRequest request) {
        return new CreateUserCommand(request.username(), request.email(), request.password());
    }
}
