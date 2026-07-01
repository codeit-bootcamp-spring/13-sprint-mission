package com.sprint.mission.discodeit.dto.binarycontent;

import com.sprint.mission.discodeit.entity.ContentType;
import io.swagger.v3.oas.annotations.media.Schema;


public record BinaryContentCreateRequest(
        @Schema(description = "파일 이름", example = "filename.png", requiredMode = Schema.RequiredMode.REQUIRED)
        String fileName,
        @Schema(description = "파일 사이즈", example = "1024", requiredMode = Schema.RequiredMode.REQUIRED)
        long fileSize,
        @Schema(description = "파일 확장자", example = "IMAGE_PNG", requiredMode = Schema.RequiredMode.REQUIRED)
        ContentType contentType,
        @Schema(description = "파일 바이트 데이터", type = "string", format = "binary", requiredMode = Schema.RequiredMode.REQUIRED)
        byte[] bytes
)
{
    private static final long BYTES_PER_MB = 1024 * 1024;
    private static final long MAX_FILE_MB = 5;
    private static final long MAX_FILE_SIZE = BYTES_PER_MB * MAX_FILE_MB;

    public BinaryContentCreateRequest{
        //파일 크기 검증
        if (fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 5MB 이하여야 합니다.");
        }
    }
}
