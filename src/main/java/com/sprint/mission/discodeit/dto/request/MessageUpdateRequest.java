package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MessageUpdateRequest(

        @NotBlank(message = "content는 비워둘 수 없습니다.")
        String newContent
) {
}
