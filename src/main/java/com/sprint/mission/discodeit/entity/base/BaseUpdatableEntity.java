package com.sprint.mission.discodeit.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

// Channel, Message, User, ReadStatus, UserStatus 가 공통으로 가지는 수정 시각을 한곳에 모은 상위 클래스
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@MappedSuperclass // 이 클래스 자체는 테이블이 되지 않는 대신 이 클래스를 상속한 엔티티의 테이블에 여기 선언된 컬럼이 합쳐져 들어간다
public abstract class BaseUpdatableEntity extends BaseEntity {

  // 수정 시각 - 저장될 때마다 현재 시각으로 갱신
  @LastModifiedDate
  @Column(columnDefinition = "timestamp with time zone") // 테이블에 매핑되는 컬럼 정보 정의 추가
  private Instant updatedAt;
}
