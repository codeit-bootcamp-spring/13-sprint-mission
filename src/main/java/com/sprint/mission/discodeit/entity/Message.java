package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Getter
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private UUID id;
    private Long createdAt;
    private Long updatedAt;
    private String content;
    private UUID authorId;
    private UUID channelId;
    private List<UUID> attachmentIds;



    public Message(String message, UUID authorId,  UUID channelId) {
        this.id = UUID.randomUUID();//쓴 글에 고유 id
        this.createdAt = Instant.now().getEpochSecond(); //글쓴 시간
        this.updatedAt = this.createdAt;//최초 업데이트 시간
        this.content = message;//메세지 내용이
        this.authorId = authorId;//글쓴 사람
        this.channelId = channelId;//작성한 채널
        this.attachmentIds = new ArrayList<>();
    }

    public void updateAttachmentIds(List<UUID> attachmentIds) {
        this.attachmentIds = attachmentIds;
    }

    public void updateMessage(String newMessage) {
       boolean anyValueUpdated = false;
       if(newMessage != null && !newMessage.equals(this.content)) {
           this.content = newMessage;
           anyValueUpdated = true;
       }
       if(anyValueUpdated) {
           this.updatedAt = Instant.now().getEpochSecond();
       }


    }

}
