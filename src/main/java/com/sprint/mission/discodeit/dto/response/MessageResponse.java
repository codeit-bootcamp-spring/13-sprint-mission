package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;

import java.util.List;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID userId,
        UUID channelId,
        String content,
        List<BinaryContentCreateRequest> attachment
) {
}
