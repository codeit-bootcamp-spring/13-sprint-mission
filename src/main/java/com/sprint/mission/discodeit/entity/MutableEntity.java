package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;

@Getter
public class MutableEntity extends BaseEntity {

    private Instant updateAt;

    public MutableEntity() {
        this.updateAt = Instant.now();
    }

    public void updateTime() {
        this.updateAt = Instant.now();
    }
}
