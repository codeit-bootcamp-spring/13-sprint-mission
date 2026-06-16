package com.sprint.mission.discodeit.dto.request;

public record CreateProfileImageRequest (
        String filename,
        String contentType,
        byte[] bytes
) {
}