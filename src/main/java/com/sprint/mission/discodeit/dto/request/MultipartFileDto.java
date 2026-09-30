package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MultipartFileDto(
        @NotBlank String filename,
        @NotBlank String contentType,
        @NotNull Long size,
        // 사용자 측 데이터 저장.
        // 데이터가 커지면, 임시파일 리스트로 저장.
        @NotNull byte[] content
) {
}
