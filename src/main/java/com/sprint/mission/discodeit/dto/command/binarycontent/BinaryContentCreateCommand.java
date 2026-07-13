package com.sprint.mission.discodeit.dto.command.binarycontent;

public record BinaryContentCreateCommand(
        String fileName,
        long fileSize,
        String contentType,
        byte[] bytes
) {
}
