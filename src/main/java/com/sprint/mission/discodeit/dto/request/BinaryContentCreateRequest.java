package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record BinaryContentCreateRequest(

        @NotBlank(message = "파일 이름은 필수입니다.")
        @Size(max = 255, message = "파일 이름은 255자 이하로 입력해주세요.")
        String fileName,

        @NotBlank(message = "파일 타입은 필수입니다.")
        String contentType,

        @NotNull(message = "파일 크기는 필수입니다.")
        @Positive(message = "파일 크기는 0보다 커야 합니다.")
        Long size,

        @NotEmpty(message = "파일 내용이 비어있습니다.")
        byte[] bytes
) {
}
