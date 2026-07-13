package com.sprint.mission.discodeit.dto.command.userstatus;

import java.util.UUID;

public record UserStatusCreateCommand(
        UUID userId
) {
}
