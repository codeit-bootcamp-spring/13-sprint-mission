package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PublicChannelUpdateRequest(

        @NotBlank(message = "name은 비워둘 수 없습니다.")
        String newName,

        @NotBlank(message = "description은 비워둘 수 없습니다.")
        String newDescription
) {
}
