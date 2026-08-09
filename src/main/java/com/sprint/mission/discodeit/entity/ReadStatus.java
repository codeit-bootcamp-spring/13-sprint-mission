package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import com.sprint.mission.discodeit.exception.NoChangesException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import java.time.Instant;
import java.util.*;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "read_statuses",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "channel_id"}))
// 여러 컬럼에 대한 UNIQUE 제약조건
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReadStatus extends BaseUpdatableEntity {

  @JoinColumn(name = "user_id", columnDefinition = "uuid")
  @ManyToOne(fetch = FetchType.LAZY, optional = false) // ReadStatus에서 user에 대한 필수적인 관계 표시
  private User user;

  @JoinColumn(name = "channel_id", columnDefinition = "uuid")
  @ManyToOne(fetch = FetchType.LAZY, optional = false) // ReadStatus에서 channel에 대한 필수적인 관계 표시
  private Channel channel;

  @Column(columnDefinition = "timestamp with time zone", nullable = false)
  private Instant lastReadAt; // ReadAt -> lastReadAt으로 변경하여 마지막으로 읽은 시간임을 알려준다
  // 채널 별 마지막으로 메시지 읽은 시간 표현
  // 사용자별 각 채널에 읽지 않은 메시지 확인하기 위해 활용

  public ReadStatus(User user, Channel channel, Instant lastReadAt) {
    this.user = user;
    this.channel = channel;
    this.lastReadAt = lastReadAt;
  }

  public void update(Instant newLastReadAt) {
    boolean anyValueUpdated = false;
    if (newLastReadAt != null && !newLastReadAt.equals(this.lastReadAt)) {
      this.lastReadAt = newLastReadAt;
      anyValueUpdated = true;
    }
    if (!anyValueUpdated) {
      throw new NoChangesException();
    }
  }
}


