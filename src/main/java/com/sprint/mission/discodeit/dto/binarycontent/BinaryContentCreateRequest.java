package com.sprint.mission.discodeit.dto.binarycontent;

import com.sprint.mission.discodeit.dto.command.binarycontent.BinaryContentCreateCommand;
import com.sprint.mission.discodeit.entity.ContentType;
import com.sprint.mission.discodeit.exception.binarycontent.InvalidFileSizeException;
import com.sprint.mission.discodeit.exception.binarycontent.UnsupportedFileTypeException;
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
            throw InvalidFileSizeException.withSize(fileSize);
        }
        if (!ContentType.isSupported(contentType)) {
            throw UnsupportedFileTypeException.withType(contentType);
        }
    }

    public BinaryContentCreateCommand toCommand() {
        return new BinaryContentCreateCommand(fileName, fileSize, contentType, bytes);
    }
}
