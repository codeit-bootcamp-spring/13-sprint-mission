package com.sprint.mission.discodeit.dto.command.user;

public record UserUpdateCommand(
        String newUsername,
        String newEmail,
        String newPassword
) {
}
