package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class BinaryContent {

    //필드
    private final UUID id;
    private final Instant createdAt;
    private final String contentPath;

    //ctor
    public BinaryContent(String contentPath) {
        id = UUID.randomUUID();
        createdAt = Instant.now();
        this.contentPath = contentPath;
    }

}
