package com.sprint.mission.discodeit.dto.input;

import java.util.UUID;


public record MessageCreateRequest(
        String content,
        UUID channelId,
        UUID authorId
){}
