package com.sprint.mission.discodeit.exception;

import java.time.Instant;

public record GlobalException(
        Instant timestamp,
        int status,
        String error,
        String message
) {
}
