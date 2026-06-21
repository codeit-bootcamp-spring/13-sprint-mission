package com.sprint.mission.discodeit.dto;

import java.util.UUID;

public class CreateMessageRequest {

    private String content;
    private UUID userId;
    private UUID channelId;

    public String getContent() {
        return content;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getChannelId() {
        return channelId;
    }
}