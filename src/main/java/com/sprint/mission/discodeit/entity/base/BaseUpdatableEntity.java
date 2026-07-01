package com.sprint.mission.discodeit.entity.base;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseUpdatableEntity extends BaseEntity {

    //필드
    @LastModifiedDate
    private Instant updatedAt;

    //ctor
    public BaseUpdatableEntity() {
        super();
        updatedAt = getCreatedAt();
    }

    //updateMethod
    public void updateUpdatedAt(){
        updatedAt = Instant.now();
    }

}
