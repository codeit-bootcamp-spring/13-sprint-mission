package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class BaseEntity implements Serializable {

    private final UUID id;
    private final Instant createAt;

    public BaseEntity() {
        this.id = UUID.randomUUID();
        this.createAt = Instant.now();
    }
}
