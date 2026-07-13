package com.sprint.mission.discodeit.dto.command.readstatus;

import java.util.UUID;

public record ReadStatusCreateCommand(
        UUID userId,
        UUID channelId
) {
}
