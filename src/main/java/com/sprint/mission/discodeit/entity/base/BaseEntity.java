package com.sprint.mission.discodeit.entity.base;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
  //Entity의 공통필드를 여기서 처리함.

  @Id //어떤 컬럼의 PK인지 알아야 JPA가 엔티티를 테이블에서 행으로 만듬.
  // -> id컬럼이 pk라고 알려주는 어노태이션임
  @GeneratedValue(strategy = GenerationType.UUID)//JPA가 UUID 생성 후 할당.
  private UUID id;

  @CreatedDate//객체가 생성 되었을때 자동으로 시간 할당.
  private Instant createdAt;

}
