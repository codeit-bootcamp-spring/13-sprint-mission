package com.sprint.mission.discodeit.entity;

import java.util.*;

public class Message extends BaseEntity {

    private String content; // 메세지 내용
    private UUID authorId; // 메세지를 작성한 유저
    private UUID channelId; // 어느 채널의 메세지


    public Message(String content, UUID authorId, UUID channelId) {
        super();
        this.content = messageContent(content);
        this.authorId = validateAuthorId(authorId);
        this.channelId = validatechannelId(channelId);
    }

    public UUID getChannelId() {
        return channelId;
    }

    private UUID validatechannelId(UUID channelId) {
        if (channelId == null) {
            throw new IllegalArgumentException("채널 ID 작성은 필수입니다.");
        }
        return channelId;
    }


    public UUID getAuthorId() {
        return authorId;
    }

    private UUID validateAuthorId(UUID authorId) {
        if (authorId == null) {
            throw new IllegalArgumentException("작성자 ID는 필수입니다.");
        }
        return authorId;
    }

    public String getContent() {
        return content;
    }

    private String messageContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("메세지의 내용이 없습니다.");
        }
        return content;
    }

    @Override
    public String toString() {
        return "Message: " +
                "보낸 채널 이름 = " + channelId +
                ", 보낸 사람 = " + authorId +
                ", 내용 = '" + content + '\'';
    }
}
