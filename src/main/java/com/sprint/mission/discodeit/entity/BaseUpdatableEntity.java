package com.sprint.mission.discodeit.entity;


import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Setter
@Getter
@MappedSuperclass
public abstract class BaseUpdatableEntity extends BaseEntity {
    @Column()
    @LastModifiedDate
    private Instant updatedAt;

    protected BaseUpdatableEntity() {
        super();
        this.updatedAt = Instant.now();
    }
}
