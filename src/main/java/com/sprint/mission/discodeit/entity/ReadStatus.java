package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "read_statuses")
@Getter
public class ReadStatus extends BaseUpdatableEntity {

  private UUID userId;
  private UUID channelId;
  private Instant lastReadAt;

  public ReadStatus() {
  }

  public ReadStatus(UUID userId, UUID channelId, Instant lastReadAt) {
    this.userId = userId;
    this.channelId = channelId;
    this.lastReadAt = lastReadAt;
  }

  public void updateLastReadAt(Instant lastReadAt) {
    this.lastReadAt = lastReadAt;
  }


}
