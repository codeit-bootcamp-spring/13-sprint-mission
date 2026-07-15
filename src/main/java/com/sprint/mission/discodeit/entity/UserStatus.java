package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_statuses")
public class UserStatus extends BaseUpdatableEntity {

  // UUID userId → User 객체로 직접 참조
  @OneToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @Column(nullable = false)
  private Instant lastActiveAt;

  public UserStatus(User user, Instant lastActiveAt) {
    this.user = user;
    this.lastActiveAt = lastActiveAt;
  }

  public void update(Instant newLastActiveAt) {
    if (newLastActiveAt != null && !newLastActiveAt.equals(this.lastActiveAt)) {
      this.lastActiveAt = newLastActiveAt;
    }
  }

  public Boolean isOnline() {
    return lastActiveAt.isAfter(Instant.now().minus(Duration.ofMinutes(5)));
  }
}