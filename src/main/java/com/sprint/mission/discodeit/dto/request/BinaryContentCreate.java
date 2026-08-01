package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BinaryContentCreate(
        @NotBlank String filename,
        @NotBlank String contentType,
        @NotNull Long size,
        @NotNull byte[] content
) {
}
