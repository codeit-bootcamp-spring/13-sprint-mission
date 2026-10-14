package com.sprint.mission.discodeit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Entity
@Table(name = "read_status", uniqueConstraints =
    @UniqueConstraint(name = "uk_read_status_user_channel", columnNames = {"user_id", "channel_id"}))
public class ReadStatus extends BaseUpdatableEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "channel_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Channel channel;

  @Column(nullable = false)
  private Instant lastReadAt;

  @Column(nullable = false)
  private boolean notificationEnabled;

  @Builder
  public ReadStatus(User user, Channel channel, Instant lastReadAt) {
    this.user = user;
    this.channel = channel;
    this.lastReadAt = lastReadAt == null ? Instant.now() : lastReadAt;
    this.notificationEnabled = channel.getType() == ChannelType.PRIVATE;
  }

  protected ReadStatus() {
  }

  public void updateLastReadAt(Instant lastReadAt) {
    if (lastReadAt != null) {
      this.lastReadAt = lastReadAt;
    }
  }

  public void updateNotificationEnabled(Boolean notificationEnabled) {
    if (notificationEnabled != null) {
      this.notificationEnabled = notificationEnabled;
    }
  }
}
