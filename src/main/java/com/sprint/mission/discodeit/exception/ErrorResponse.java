package com.sprint.mission.discodeit.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        String code,
        String message,
        Map<String, Object> details,
        String exceptionType,
        int status
) {

    public static ErrorResponse of(DiscodeitException e){
        ErrorCode ec = e.getErrorCode();
        return new ErrorResponse(
                e.getTimestamp(),
                ec.name(),
                e.getMessage(),
                e.getDetails(),
                e.getClass().getSimpleName(),
                ec.getStatus().value()
        );
    }
}
