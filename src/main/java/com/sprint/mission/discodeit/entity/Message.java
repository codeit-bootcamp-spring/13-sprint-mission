package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Getter
public class Message extends BaseUpdatableEntity {


  private String content;
  private UUID authorId;
  private UUID channelId;
  private List<UUID> attachmentIds;

  public Message() {
  }

  public Message(String message, UUID authorId, UUID channelId) {
    this.content = message;//메세지 내용이
    this.authorId = authorId;//글쓴 사람
    this.channelId = channelId;//작성한 채널
    this.attachmentIds = new ArrayList<>();
  }

  public void updateAttachmentIds(List<UUID> attachmentIds) {
    this.attachmentIds = attachmentIds;
  }

  public void updateMessage(String newMessage) {

    if (newMessage != null && !newMessage.equals(this.content)) {
      this.content = newMessage;
    }


  }

}
