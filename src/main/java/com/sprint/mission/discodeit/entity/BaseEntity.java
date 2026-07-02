package com.sprint.mission.discodeit.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public abstract class BaseEntity {
    private final UUID id = UUID.randomUUID();
    @CreatedDate
    private final Instant createdAt = Instant.now();
}
