package com.sprint.mission.discodeit.dto.binarycontent;

import com.sprint.mission.discodeit.entity.ContentType;


public record BinaryContentCreateRequest(
        String fileName,
        long fileSize,
        ContentType contentType,
        byte[] bytes
)
{
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; //5MB

    public BinaryContentCreateRequest{
        //파일 크기 검증
        if (fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 5MB 이하여야 합니다.");
        }
    }

}
