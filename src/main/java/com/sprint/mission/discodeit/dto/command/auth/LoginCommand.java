package com.sprint.mission.discodeit.dto.command.auth;

public record LoginCommand(
        String username,
        String password
) {
}
