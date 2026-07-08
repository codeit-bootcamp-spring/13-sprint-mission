package com.sprint.mission.discodeit.entity.base;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

// Channel, Message, User, ReadStatus, UserStatus 가 공통으로 가지는 수정 시각을 한곳에 모은 상위 클래스
@Getter
@MappedSuperclass // 이 클래스 자체는 테이블이 되지 않는 대신 이 클래스를 상속한 엔티티의 테이블에 여기 선언된 컬럼이 합쳐져 들어간다
@EntityListeners(AuditingEntityListener.class)
// JPA Auditing - 시각 자동으로 기록하기 위해 추가, @EnableJpaAuditing과 짝
public abstract class BaseUpdatableEntity extends BaseEntity {

  // 수정 시각 - 저장될 때마다 현재 시각으로 갱신
  @LastModifiedDate
  private Instant updatedAt;
}
