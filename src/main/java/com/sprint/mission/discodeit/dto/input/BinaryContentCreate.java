package com.sprint.mission.discodeit.dto.input;

public record BinaryContentCreate(
        String filename,
        String contentType,
        Long size,
        byte[] content
) {
}
