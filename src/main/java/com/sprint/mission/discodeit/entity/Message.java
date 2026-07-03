package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import lombok.Getter;
import lombok.ToString;

import java.util.List;


@Getter
@ToString
public class Message extends BaseUpdatableEntity{

    private String content;
    private  Channel channel;
    private  User author;
    private  List<BinaryContent> attachments;

    // 텍스트 + 첨부파일
    public Message(String content, Channel channel, User author, List<BinaryContent> attachments) {
        super();
        this.content = content;
        this.channel = channel;
        this.author = author;
        this.attachments = attachments == null ? List.of() : List.copyOf(attachments);
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
