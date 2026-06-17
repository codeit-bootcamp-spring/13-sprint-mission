package com.sprint.mission.discodeit.dto.request;

import java.util.UUID;

public record CreateMessageRequest(
        String content,
        UUID channelId,
        UUID authorId
) {
}
