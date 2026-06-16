package com.sprint.mission.discodeit.dto.request;

import java.util.UUID;

public record BinaryContentRequest(
        UUID userId,
        UUID messageId,
        String fileName
) {}
