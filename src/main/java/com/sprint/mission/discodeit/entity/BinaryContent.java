package com.sprint.mission.discodeit.entity;

import static java.util.Objects.requireNonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Entity
@Table(name = "binary_content")
public class BinaryContent extends BaseUpdatableEntity {

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private BinaryContentStatus status = BinaryContentStatus.PROCESSING;

  @Column(nullable = false, length = 255)
  private String fileName;

  @Column(nullable = false)
  private Long size;

  @Column(nullable = false, length = 100)
  private String contentType;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", unique = true)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "message_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Message message;

  protected BinaryContent() {
  }

  @Builder
  public BinaryContent(String fileName, Long size, String contentType, User user, Message message) {
    this.fileName = fileName;
    this.size = size;
    this.contentType = contentType;
    this.user = user;
    this.message = message;
  }

  public void updateStatus(BinaryContentStatus status) {
    this.status = requireNonNull(status, "status must not be null");
  }
}
