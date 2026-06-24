package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class MessageResponse {

    private UUID id;
    private String content;
    private UUID channelId;
    private UUID authorId;
    private Instant createdAt;
    private List<BinaryContentResponse> attachments;

    public static MessageResponse from(
            Message message,
            List<BinaryContentResponse> attachments) {
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
