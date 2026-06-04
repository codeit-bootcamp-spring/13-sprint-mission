package com.sprint.mission.discodeit.entity;

import lombok.*;

import java.io.*;
import java.util.*;

@Getter
public class Message extends BaseEntity implements Serializable {

    private String content; // 메세지 내용
    private UUID authorId; // 메세지를 작성한 유저
    private UUID channelId; // 어느 채널의 메세지

    public Message(String content, UUID channelId, UUID authorId) {

        validateContent(content);
        this.content = content;

        validateChannelId(channelId);
        this.channelId = channelId;

        validateAuthorId(authorId);
        this.authorId = authorId;
    }

    private void validateChannelId(UUID channelId) {
        if (channelId == null) {
            throw new IllegalArgumentException("채널 ID 작성은 필수입니다.");
        }
    }

    private void validateAuthorId(UUID authorId) {
        if (authorId == null) {
            throw new IllegalArgumentException("작성자 ID는 필수입니다.");
        }
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("메세지의 내용이 없습니다.");
        }
    }

    public void updateContent(String content) {
        validateContent(content);

        if (content.equals(this.content)) {
            return;
        }

        this.content = content;
        setUpdatedAt();
    }

    @Override
    public String toString() {
        return "Message: " +
                "보낸 채널 이름 = " + channelId +
                ", 보낸 사람 = " + authorId +
                ", 내용 = '" + content + '\'';
    }
}
