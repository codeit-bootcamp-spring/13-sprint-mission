package com.sprint.mission.discodeit.dto.input;

public record UserUpdateRequest(
        String newUsername,
        String newEmail,
        String newPassword
) {}
