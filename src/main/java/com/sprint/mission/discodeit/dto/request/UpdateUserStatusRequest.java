package com.sprint.mission.discodeit.dto.request;

import java.time.*;

public record UpdateUserStatusRequest(
        Instant lastOnlineTime
) {
}
