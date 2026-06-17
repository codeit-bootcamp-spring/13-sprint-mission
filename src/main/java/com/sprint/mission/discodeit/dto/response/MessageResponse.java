package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.Message;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        String content,
        UUID channelId,
        UUID authorId,
        Instant createdAt,
        List<BinaryContentResponse> attachments
) {
    public static MessageResponse from(
            Message message,
            List<BinaryContentResponse> attachments
    ) {
        return new MessageResponse(
                message.getId(),
                message.getContent(),
                message.getChannelId(),
                message.getAuthorId(),
                message.getCreatedAt(),
                attachments
        );
    }
}
