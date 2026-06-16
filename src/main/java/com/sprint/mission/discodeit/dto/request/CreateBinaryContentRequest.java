package com.sprint.mission.discodeit.dto.request;

public record CreateBinaryContentRequest(
        String filename,
        String contentType,
        byte[] bytes
) {
}
