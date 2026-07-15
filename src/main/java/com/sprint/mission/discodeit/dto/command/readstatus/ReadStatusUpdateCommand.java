package com.sprint.mission.discodeit.dto.command.readstatus;

import java.time.Instant;

public record ReadStatusUpdateCommand(
        Instant newLastReadAt
) {
}
