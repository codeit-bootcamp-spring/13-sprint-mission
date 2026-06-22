package com.sprint.mission.discodeit.entity;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class BinaryContent implements Serializable {

  private static final long serialVersionUID = 1L;

  private UUID id;
  private Instant createdAt;
  private Instant updatedAt;
  private UUID messageId;

  private String fileName;
  private String fileUrl;
  private Long fileSize;

  @Builder
  private BinaryContent(UUID id, Instant createdAt, Instant updatedAt, UUID messageId
      , String fileName, String fileUrl, Long fileSize) {
    this.id = id;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.fileName = fileName;
    this.fileUrl = fileUrl;
    this.fileSize = fileSize;
    this.messageId = messageId;
  }

  public void updateMessageId(UUID messageId) {
    this.messageId = messageId;
  }
}

