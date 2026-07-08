package com.sprint.mission.discodeit.entity.basic;

import lombok.*;
import org.springframework.data.annotation.*;

import java.time.*;
import java.util.*;

@Getter
public class BaseEntity {

    protected UUID id;

    @CreatedDate
    protected Instant createdAt;

    protected BaseEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
    }

}
