package com.sprint.mission.discodeit.entity;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class ReadStatus implements Serializable {

  private static final long serialVersionUID = 1L;

  private UUID id;
  private Instant createdAt;
  private Instant updatedAt;
  //
  private UUID userId;
  private UUID channelId;
  private Instant readAt;

  @Builder
  public ReadStatus(UUID id, Instant createdAt, Instant updatedAt, UUID userId, UUID channelId,
      Instant readAt) {
    this.id = id;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.userId = userId;
    this.channelId = channelId;
    this.readAt = readAt;

  }

  public void update(Instant newLastReadAt) {
    this.readAt = newLastReadAt;
    this.updatedAt = Instant.now();
  }

}
