package com.sprint.mission.discodeit.dto.binarycontent;

import com.sprint.mission.discodeit.entity.BinaryContentStatus;
import java.util.UUID;

public record BinaryContentResponse(
    UUID id,
    String fileName,
    Long size,
    String contentType,
    BinaryContentStatus status
) {
}
