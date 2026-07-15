package com.sprint.mission.discodeit.entity.base;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.*;

import java.time.*;

@Getter
@MappedSuperclass
public abstract class BaseUpdatableEntity extends BaseEntity {

    @LastModifiedDate
    protected Instant updatedAt;

    protected BaseUpdatableEntity() {
        super();
    }

    protected void setUpdatedAt() {
        this.updatedAt = Instant.now();
    }


}
