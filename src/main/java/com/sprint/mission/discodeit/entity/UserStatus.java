package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "user_statuses")
public class UserStatus extends BaseUpdatableEntity {

  @OneToOne
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @Column(name = "last_active_at", nullable = false)
  private Instant lastActiveAt;

  public UserStatus(User user, Instant lastActiveAt) {
    this.user = user;
    this.lastActiveAt = lastActiveAt;
  }

  public boolean isOnline() {
    if (this.lastActiveAt == null) {
      return false;
    }

    Instant fiveMinuteAgo = Instant.now().minus(java.time.Duration.ofMinutes(5));
    return lastActiveAt.isAfter(fiveMinuteAgo);
  }

  public void updateOnlineStatus(Instant newLastActiveAt) {
    this.lastActiveAt = newLastActiveAt;
  }

  public void updateActiveTime() {
    this.lastActiveAt = Instant.now();
  }
}
