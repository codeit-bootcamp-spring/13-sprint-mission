package com.sprint.mission.discodeit.entity.base;

import java.time.Instant;
import org.springframework.data.annotation.LastModifiedDate;


public abstract class BaseUpdatableEntity extends BaseEntity {

  @LastModifiedDate//객체 수정시 자동으로 시간 할당
  private Instant updatedAt;

}
