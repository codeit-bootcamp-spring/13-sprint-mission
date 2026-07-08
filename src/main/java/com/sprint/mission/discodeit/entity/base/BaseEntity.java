package com.sprint.mission.discodeit.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass // 이 클래스 자체는 테이블이 되지 않는 대신 이 클래스를 상속한 엔티티의 테이블에 여기 선언된 컬럼이 합쳐져 들어간다
@EntityListeners(AuditingEntityListener.class)
// JPA Auditing - 시각 자동으로 기록하기 위해 추가, @EnableJpaAuditing과 짝
public abstract class BaseEntity { // 모든 엔티티가 공통으로 가지는 식별자(id)와 생성 시각을 한 곳에 모은 상위 클래스

  @Id // 테이블 기본 키 알려준다, primary key
  @GeneratedValue(strategy = GenerationType.UUID) // 숫자 자동 전략
  private UUID id;

  // 생성 시각은 처음 저장될 때 한 번 채워지고, 이후 바뀌지 않는다
  // insert 되는 시간 자동으로 채워진다
  @CreatedDate
  @Column(nullable = false, updatable = false) // 필수값(빈 값 불가)
  private Instant createdAt;
}
