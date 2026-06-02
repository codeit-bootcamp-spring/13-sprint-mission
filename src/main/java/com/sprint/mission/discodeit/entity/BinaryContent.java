package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
public class BinaryContent {

    private UUID id;
    private Instant createdAt;

    private UUID profileId;
    private List<UUID> attachmentIds;

}
