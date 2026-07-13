package com.sprint.mission.discodeit.dto.command.message;

import java.util.UUID;

public record MessageCreateCommand(
        UUID channelId,
        UUID authorId,
        String content
) {
}
