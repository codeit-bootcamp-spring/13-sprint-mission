package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.Message;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageUpdateResponse(
        UUID messageId,
        Instant createdAt,
        Instant updatedAt,
        String content,
        UUID channelId,
        UUID authorId,
        List<UUID> attachmentIds
) {
    public static MessageUpdateResponse from(Message message) {
        return new MessageUpdateResponse(
                message.getId(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                message.getContent(),
                message.getChannelId(),
                message.getAuthorId(),
                message.getAttachmentIds()
        );
    }

}

