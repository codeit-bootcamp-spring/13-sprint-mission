package com.sprint.mission.discodeit.dto.command.user;

public record UserCreateCommand(
        String username,
        String email,
        String password
) {
}
