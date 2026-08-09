package com.sprint.mission.discodeit.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import com.sprint.mission.discodeit.exception.NoChangesException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import java.time.Duration;
import java.time.Instant;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_statuses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStatus extends BaseUpdatableEntity {

  @JsonBackReference // Jackson 통한 직렬화 시 순환 참조 방지
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  @OneToOne(fetch = FetchType.LAZY, optional = false) // UserStatus에서 User 관계는 반드시 존재해야 한다
  private User user;

  @Column(columnDefinition = "timestamp with time zone", nullable = false)
  private Instant lastActiveAt;

  // 사용자 별 마지막으로 확인된 접속 시간을 표현하는 도메인 모델
  // 사용자의 온라인 상태 확인하기 위해 활용
  // 마지막 접속 시간 기준으로 현재 로그인한 유저로 판단할 수 있는 메소드 정의
  // 마지막 접속 시간이 현재 시간으로부터 5분 이내이면 현재 접속 중인 유저로 간주

  public UserStatus(User user, Instant lastActiveAt) {
    setUser(user); // user <-> userStatus 양방향 참조관계
    this.lastActiveAt = lastActiveAt;
  }

  protected void setUser(User user) { // User <- status -> UserStatus
    this.user = user;
    user.setStatus(this);
  }

  public void update(Instant lastActiveAt) {
    boolean anyValueUpdated = false;
    if (lastActiveAt != null && !lastActiveAt.equals(this.lastActiveAt)) {
      this.lastActiveAt = lastActiveAt;
      anyValueUpdated = true;
    }
    if (!anyValueUpdated) {
      throw new NoChangesException();
    }
  }

  // 사용자가 현재 온라인 유저인지 판정하는 메소드 추가
  public boolean isOnline() {
    Instant instantFiveMinutesAgo = Instant.now().minus(Duration.ofMinutes(5));
    // 값이 5분 이내라면 온라인 유저로 간주
    return lastActiveAt.isAfter(instantFiveMinutesAgo);
  }
}
