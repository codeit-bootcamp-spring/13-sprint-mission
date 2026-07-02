package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public abstract class BaseEntity {
    private final UUID id;
    @CreatedDate
    private final Instant createdAt;

    public BaseEntity() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
    }
}
