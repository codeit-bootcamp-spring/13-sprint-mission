package com.sprint.mission.discodeit.dto.command;

import com.sprint.mission.discodeit.dto.request.*;
import io.swagger.v3.oas.annotations.media.*;

import java.time.*;

public record UpdateUserStatusCommand(
        Instant lastOnlineTime
) {
    public UpdateUserStatusCommand from(UpdateUserStatusRequest request) {
        return new UpdateUserStatusCommand(request.lastOnlineTime());
    }
}
