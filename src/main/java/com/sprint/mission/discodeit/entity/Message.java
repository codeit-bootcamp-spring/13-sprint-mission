package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "messages")
@Getter
public class Message extends BaseUpdatableEntity {


  private String content;

  @ManyToOne
  @JoinColumn(name = "author_id")
  private User author;

  @ManyToOne
  @JoinColumn(name = "channel_id")
  private Channel channel;

  @ManyToMany
  @JoinTable(name = "message_attachments",//중간 테이블
      joinColumns = @JoinColumn(name = "message_id"),  //현제 엔티티의 fk
      inverseJoinColumns = @JoinColumn(name = "attachment_id")) //반대편 엔티티의 fk
  //N:M 관계를 테이블 3개를 연결
  private List<BinaryContent> attachments;

  public Message() {
  }

  public Message(String message, User author, Channel channel) {
    this.content = message;//메세지 내용이
    this.author = author;//글쓴 사람
    this.channel = channel;//작성한 채널
    this.attachments = new ArrayList<>();
  }

  public void updateAttachmentIds(List<BinaryContent> attachments) {
    this.attachments = attachments;
  }

  public void updateMessage(String newMessage) {

    if (newMessage != null && !newMessage.equals(this.content)) {
      this.content = newMessage;
    }


  }

}
