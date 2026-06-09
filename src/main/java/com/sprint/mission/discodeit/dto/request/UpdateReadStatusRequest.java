package com.sprint.mission.discodeit.dto.request;

import java.time.*;

public record UpdateReadStatusRequest  (
        Instant lastReadTime
) {
}
