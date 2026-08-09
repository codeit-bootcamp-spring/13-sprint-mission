package com.sprint.mission.discodeit.dto.command;

import com.sprint.mission.discodeit.dto.request.*;

import java.time.*;
import java.util.*;

public record CreateReadStatusCommand(
        UUID userId,
        UUID channelId,
        Instant lastReadAt
) {

    public static CreateReadStatusCommand from(CreateReadStatusRequest request) {
        return new CreateReadStatusCommand(request.userId(), request.channelId(), request.lastReadAt());
    }
}

