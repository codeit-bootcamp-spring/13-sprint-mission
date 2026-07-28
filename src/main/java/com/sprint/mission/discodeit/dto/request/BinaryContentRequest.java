package com.sprint.mission.discodeit.dto.request;

import java.util.UUID;

public record BinaryContentRequest(
        String fileName,
        Long size,
        String contentType,
        byte[] bytes
) {}
