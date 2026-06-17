package com.sprint.mission.discodeit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class MessageResponse {

    private UUID id;

    private String content;

    private UUID channelId;

    private UUID authorId;

    private Instant createdAt;
}