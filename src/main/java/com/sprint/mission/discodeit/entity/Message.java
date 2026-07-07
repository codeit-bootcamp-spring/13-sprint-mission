package com.sprint.mission.discodeit.entity;

import lombok.*;

import java.io.*;
import java.util.*;

@Getter
public class Message extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private String content;
    private UUID authorId;
    private UUID channelId;
    private List<UUID> attachmentIds;

    public Message(String content, UUID channelId, UUID authorId, List<UUID> attachmentIds) {
        super();
        this.content = content;
        this.channelId = channelId;
        this.authorId = authorId;
        this.attachmentIds = attachmentIds;
    }

    public void update(String content) {
        boolean anyValueUpdated = false;
        if (content != null && content.equals(this.content)) {
            this.content = content;
            anyValueUpdated = true;
        }
        if (anyValueUpdated) {
            setUpdatedAt();
        }
    }

}
