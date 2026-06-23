package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UserUpdateRequest(

        @NotBlank(message = "newName은 비워둘 수 없습니다.")
        String newUsername,

        @NotBlank(message = "newEmail은 비워둘 수 없습니다.")
        String newEmail,

        @NotBlank(message = "newPassword은 비워둘 수 없습니다.")
        String newPassword
) {

}
