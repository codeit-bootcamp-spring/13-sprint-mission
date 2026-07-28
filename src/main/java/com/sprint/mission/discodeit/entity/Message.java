package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import com.sprint.mission.discodeit.exception.NoChangesException;
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

  @JoinColumn(name = "channel_id", columnDefinition = "uuid")
  @ManyToOne(fetch = FetchType.LAZY, optional = false) // 필수적인 관계 - false 설정
  private Channel channel;

  @JoinColumn(name = "author_id", columnDefinition = "uuid")
  @ManyToOne(fetch = FetchType.LAZY)
  private User author;

  @Column(columnDefinition = "text", nullable = false)
  private String content;

  @JoinTable(
      name = "message_attachments",
      joinColumns = @JoinColumn(name = "message_id"),
      inverseJoinColumns = @JoinColumn(name = "attachment_id"))
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  // CascadeType.ALL 삭제 또한 전이, 자식 생명주기 부모에 완전히 종속
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
      throw new NoChangesException();
    }
  }
}
