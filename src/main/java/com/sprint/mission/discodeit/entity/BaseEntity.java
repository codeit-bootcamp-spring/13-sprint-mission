package com.sprint.mission.discodeit.entity;

import lombok.*;import java.time.*;import java.util.*;

@Getter
public class BaseEntity {

    private final UUID id;
    private final Instant createdAt;
    private Instant updatedAt;

    BaseEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = null;
    }

    public void setUpdatedAt() {
        this.updatedAt = Instant.now();
    }

}
