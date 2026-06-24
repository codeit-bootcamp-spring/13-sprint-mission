package com.sprint.mission.discodeit.dto.message;

import com.sprint.mission.discodeit.entity.Message;
import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class MessageResponse {
    private UUID id;
    private UUID channelId;
    private UUID authorId;
    private String content;
    private List<UUID> attachmentIds;

    public static MessageResponse from(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .channelId(message.getChannelId())
                .authorId(message.getAuthorId())
                .content(message.getContent())
                .attachmentIds(message.getAttachmentIds())
                .build();
    }
}
