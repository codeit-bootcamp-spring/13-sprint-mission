package com.sprint.mission.discodeit.dto.response;

import java.time.*;
import java.util.*;

public record ReadStatusDto(
        UUID id,
        UUID userId,
        UUID channelId,
        Instant lastReadTime
) {
}