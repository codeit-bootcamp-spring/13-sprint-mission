package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Getter
public class Message extends BaseUpdatableEntity {

    //필드
    private String content;
    private final UUID channelId;
    private final UUID authorId;
    private List<UUID> attachmentIds;

    //ctor
    public Message(String content, UUID channelId, UUID authorId, List<UUID> attachmentIds) {
        super();

        this.content = content;
        this.channelId = channelId;
        this.authorId = authorId;
        this.attachmentIds = attachmentIds;
    }

    //update Method
    public void updateMessage(String content) {
        this.content = content;

        updateUpdatedAt();
    }

    //method override
    @Override
    public String toString() {
        return "User: " + authorId + ", Channel: " + channelId + "\n[Message: " + content + "]";
    }
}
