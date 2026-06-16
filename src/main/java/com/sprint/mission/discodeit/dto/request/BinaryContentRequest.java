package com.sprint.mission.discodeit.dto.request;

public record BinaryContentRequest(
        String filename,
        String contentType,
        byte[] bytes
) {
}
