package com.sprint.mission.discodeit.dto.binarycontent;

import java.util.UUID;

public record BinaryContentCreateRequest(
        UUID userId,
        String fileName,
        long fileSize,
        String contentType,
        byte[] bytes
)
{

}
