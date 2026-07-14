package com.sprint.mission.discodeit.entity.base;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.*;


import java.time.*;
import java.util.*;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    protected UUID id;

    @CreatedDate
    @Column(updatable = false)
    protected Instant createdAt;

    protected BaseEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
    }

}
