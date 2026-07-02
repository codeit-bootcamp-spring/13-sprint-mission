package com.sprint.mission.discodeit.entity;


import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class Message extends BaseUpdatableEntity {
    private String content;
    private final Channel channel;
    private final User author;
    private final List<BinaryContent> attachment;

    public Message(
            UUID id,
            Instant ctime,
            Instant mtime,
            String content,
            Channel channel,
            User author,
            List<BinaryContent> attachment) {
        super(id, ctime, mtime);
        this.content = content;
        this.channel = channel;
        this.author = author;
        this.attachment = attachment;
    }

    public Message(
            String content,
            Channel channel,
            User author,
            List<BinaryContent> attachment
    ){
        super();
        this.content = content;
        this.channel = channel;
        this.author = author;
        this.attachment = attachment;
    }
}
