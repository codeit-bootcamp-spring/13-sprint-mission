package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.util.UUID;

public abstract class EntityRoot implements Serializable {

    //필드
    private UUID id;
    private Long createdAt;
    private Long updatedAt;

    //ctor
    public EntityRoot() {
        id = UUID.randomUUID();
        createdAt = System.currentTimeMillis();
        updatedAt = createdAt;
    }

    //getter
    public UUID getId() {
        return id;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    //updateMethod
    public void updateUpdatedAt(){
        updatedAt = System.currentTimeMillis();
    }

}
