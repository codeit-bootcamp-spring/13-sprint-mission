package com.sprint.mission.discodeit.dto.request;

public record BinaryContentCreate(
        String filename,
        String contentType,
        Long size,
        byte[] content
) {
}
