package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import lombok.AccessLevel;
import lombok.Getter;
import java.util.List;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자를 protected로 선언(JPA가 사용하는 생성자)
public class Message extends BaseUpdatableEntity {

  @JoinColumn(name = "channel_id", nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  private Channel channel;

  @JoinColumn(name = "author_id")
  @ManyToOne(fetch = FetchType.LAZY)
  private User author;

  @Column
  private String content;

  @JoinTable(
      name = "message_attachments",
      joinColumns = @JoinColumn(name = "message_id"),
      inverseJoinColumns = @JoinColumn(name = "attachment_id"))
  @OneToMany(cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<BinaryContent> attachments = new ArrayList<>(); // 클래스 다이어그램에 따라 클래스 참조관계 수정

  public Message(String content, Channel channel, User author, List<BinaryContent> attachments) {
    this.content = content;
    this.channel = channel;
    this.author = author;
    this.attachments = attachments != null ? attachments : new ArrayList<>(); // NPE 방어
  }

  // 필드 수정하는 update 함수 정의
  public void update(String newContent) {
    boolean anyValueUpdated = false; // 수정시간은 실제 변경이 있을 때만 갱신되도록 구성
    if (newContent != null && !newContent.equals(this.content)) {
      this.content = newContent;
      anyValueUpdated = true;
    }
    if (!anyValueUpdated) {
      throw new IllegalArgumentException("변경사항이 없습니다!");
    }
  }
}
