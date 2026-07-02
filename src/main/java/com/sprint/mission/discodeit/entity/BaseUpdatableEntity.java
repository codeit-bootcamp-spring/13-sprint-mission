package com.sprint.mission.discodeit.entity;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
public abstract class BaseUpdatableEntity extends BaseEntity {
    @LastModifiedDate
    private Instant updatedAt;

    public BaseUpdatableEntity(
            UUID id,
            Instant ctime,
            Instant mtime
    ) {
        super(id, ctime);
        this.updatedAt = mtime;
    }

    public BaseUpdatableEntity(){
        super();
        this.updatedAt = Instant.now();
    }
}
