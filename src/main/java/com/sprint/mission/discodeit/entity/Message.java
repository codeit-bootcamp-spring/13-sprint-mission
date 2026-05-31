package com.sprint.mission.discodeit.entity;

import java.io.Serializable;

public class Message extends BaseEntity implements Serializable {
    public static final long serialVersionUID = 1L;


    private String content;
private User author;
private Channel channel;

    public Message(String content, User author, Channel channel) {
        super();
        this.content = content;
        this.author = author;
        this.channel = channel;
    }

    public String getContent() {
        return content;
    }
    public User getAuthor() {
        return author;
    }
    public Channel getChannel() {
        return channel;
    }

    public void updateContent(String content) {
        validateContent(content);
        this.content = content;
    }

    private void validateContent(String content) {
            if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("메세지 내용을 입력하세요.");
        }
    }

    @Override
    public String toString() {
        return "[" + channel.getChannelName() + " | " + author.getUsername() + "]" + '\n' +
                content + '\n';


    }
}
