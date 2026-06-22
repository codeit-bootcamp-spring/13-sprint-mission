package com.sprint.mission.discodeit.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class CreateMessageRequest {

    private String content;
    private UUID channelId;
    private UUID authorId;

    private List<CreateBinaryContentRequest> attachments;

    public CreateMessageRequest(String content, UUID channelId, UUID authorId) {
        this.content = content;
        this.channelId = channelId;
        this.authorId = authorId;
    }
}
