package com.sprint.mission.discodeit.dto.request;

import java.util.UUID;

public record UserUpdateRequest(
        UUID userId,
        String newName,
        String newEmail,
        String newPassword,
        String profileImagePath
) {

}
