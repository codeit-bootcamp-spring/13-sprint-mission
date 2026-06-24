package com.sprint.mission.discodeit.dto.binarycontent;

import com.sprint.mission.discodeit.entity.ContentType;


public record BinaryContentCreateRequest(
        String fileName,
        long fileSize,
        ContentType contentType,
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
