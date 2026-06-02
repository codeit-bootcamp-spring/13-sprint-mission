package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class EntityRoot implements Serializable {

    //필드
    private final UUID id;
    private final Instant createdAt;
    private Instant updatedAt;

    //ctor
    public EntityRoot() {
        id = UUID.randomUUID();
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    //updateMethod
    public void updateUpdatedAt(){
        updatedAt = Instant.now();
    }

}
