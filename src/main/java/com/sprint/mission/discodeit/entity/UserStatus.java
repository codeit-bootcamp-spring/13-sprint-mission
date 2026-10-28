package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;

@Entity
@Table(name = "user_statuses")
@Getter
public class UserStatus extends BaseUpdatableEntity {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @Column(name = "last_active_at")
  private Instant lastActiveAt;

  @Column(name = "online", nullable = false)
  private boolean online;

  protected UserStatus() {
  }

  public UserStatus(User user) {
    this.user = user;
    this.lastActiveAt = Instant.now();
  }

  public void updateActivity() {
    this.lastActiveAt = Instant.now();
  }

  public void updateOnline(boolean online) {
    this.online = online;

    if (online) {
      updateActivity();
    }
  }
}