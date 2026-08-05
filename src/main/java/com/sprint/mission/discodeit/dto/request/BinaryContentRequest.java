package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record BinaryContentRequest(
        @NotBlank(message = "파일 이름은 필수입니다.")
        String fileName,

        @NotNull(message = "파일 크기는 필수입니다.")
        @PositiveOrZero(message = "파일 크기는 0 이상이어야 합니다.")
        Long size,

        @NotBlank(message = "콘텐츠 타입은 필수입니다.")
        String contentType,

        @NotNull(message = "파일 데이터는 필수입니다.")
        @Size(min = 1, message = "파일 데이터는 비어 있을 수 없습니다.")
        byte[] bytes
) {}
