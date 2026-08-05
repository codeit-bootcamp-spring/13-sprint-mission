package com.sprint.mission.discodeit.dto.response;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String exceptionType,
        Map<String, Object> details
) {}
