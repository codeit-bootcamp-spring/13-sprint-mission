package com.sprint.mission.discodeit.dto.binarycontent;

import com.sprint.mission.discodeit.dto.command.binarycontent.BinaryContentCreateCommand;
import com.sprint.mission.discodeit.entity.ContentType;
import io.swagger.v3.oas.annotations.media.Schema;


public record BinaryContentCreateRequest(
        @Schema(description = "파일 이름", example = "filename.png", requiredMode = Schema.RequiredMode.REQUIRED)
        String fileName,
        @Schema(description = "파일 사이즈", example = "1024", requiredMode = Schema.RequiredMode.REQUIRED)
        long fileSize,
        @Schema(description = "파일 MIME 타입", example = "image/png", requiredMode = Schema.RequiredMode.REQUIRED)
        String contentType,
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
        if (!ContentType.isSupported(contentType)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식 입니다:" + contentType);
        }
    }

    public BinaryContentCreateCommand toCommand() {
        return new BinaryContentCreateCommand(fileName, fileSize, contentType, bytes);
    }
}
