package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BinaryContentCreateRequest(

        @NotBlank(message = "경로는 비워둘 수 없습니다.")
        String contentPath

) {
}
