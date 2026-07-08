package com.sprint.mission.discodeit.entity.basic;

import lombok.*;
import org.springframework.data.annotation.*;

import java.time.*;

@Getter
public class BaseUpdatableEntity extends BaseEntity {

    @LastModifiedBy
    protected Instant updateAt;

    protected BaseUpdatableEntity() {
        super();
    }

    protected void setUpdateAt() {
        this.updateAt = Instant.now();
    }


}
