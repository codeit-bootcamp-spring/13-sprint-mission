package com.sprint.mission.discodeit.dto.command.userstatus;

import java.time.Instant;

public record UserStatusUpdateCommand(
        Instant newLastActiveAt
) {
}
