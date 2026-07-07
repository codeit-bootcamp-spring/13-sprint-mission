package com.sprint.mission.discodeit.dto.binarycontent;

import com.sprint.mission.discodeit.entity.ContentType;

import java.time.Instant;
import java.util.UUID;

public record BinaryContentResponse(
        UUID id,
        Instant createdAt,
        String fileName,
        long size,
        ContentType contentType
)
{

}
