package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

@Getter
public abstract class EntityRoot implements Serializable {

    //필드
    private final UUID id;
    private final Long createdAt;
    private Long updatedAt;

    //ctor
    public EntityRoot() {
        id = UUID.randomUUID();
        createdAt = System.currentTimeMillis();
        updatedAt = createdAt;
    }

    //updateMethod
    public void updateUpdatedAt(){
        updatedAt = System.currentTimeMillis();
    }

}
