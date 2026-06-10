package com.sprint.mission.discodeit.dto.binarycontent;

import com.sprint.mission.discodeit.entity.ContentType;

import java.util.Arrays;
import java.util.UUID;

public record BinaryContentCreateRequest(
        UUID userId,
        String fileName,
        long fileSize,
        String contentType,
        byte[] bytes
)
{
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; //5MB

    public BinaryContentCreateRequest{
        //파일 크기 검증
        if (fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 5MB 이하여야 합니다.");
        }

        //파일 타입검증
        boolean isAllowed = Arrays.stream(ContentType.values())
                .anyMatch(ct -> ct.getValue().equals(contentType));
        if (!isAllowed) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다.");
        }
    }

}
