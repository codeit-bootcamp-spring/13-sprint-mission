package com.sprint.mission.discodeit.dto.message;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public record MessageResponse(
    UUID id,
    UUID channelId,
    UUID authorId,
    String content,
    List<UUID> attachmentIds

) {
    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getChannelId(),
                message.getAuthorId(),
                message.getContent(),
                message.getAttachmentIds()

        );
    }
}
