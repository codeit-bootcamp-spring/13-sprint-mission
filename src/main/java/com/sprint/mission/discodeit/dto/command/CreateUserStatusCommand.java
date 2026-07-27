package com.sprint.mission.discodeit.dto.command;

import com.sprint.mission.discodeit.dto.request.*;

import java.time.*;
import java.util.*;

public record CreateUserStatusCommand(
        UUID userId,
        Instant lastOnlineAt
) {

    public static CreateUserStatusCommand from(CreateUserStatusRequest request) {
        return new CreateUserStatusCommand(request.userId(), request.lastOnlineAt());
    }
}
