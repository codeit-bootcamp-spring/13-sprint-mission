package com.sprint.mission.discodeit.dto.request;

import java.time.*;
import java.util.*;

public record CreateReadStatusRequest(
        UUID userId,
        UUID channelId,
        Instant lastReadAt
) {
}

