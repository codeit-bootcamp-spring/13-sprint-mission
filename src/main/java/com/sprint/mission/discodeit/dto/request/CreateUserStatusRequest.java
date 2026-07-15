package com.sprint.mission.discodeit.dto.request;

import java.time.*;
import java.util.*;

public record CreateUserStatusRequest(
        UUID userId,
        Instant lastOnlineAt
) {
}
