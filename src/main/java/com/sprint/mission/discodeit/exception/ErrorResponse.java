package com.sprint.mission.discodeit.exception;

import java.time.*;
import java.util.*;

public record ErrorResponse(
        Instant timestamp,
        String code,
        String message,
        Map<String, Object> details,
        String exceptionType,
        int status
) {

    public static ErrorResponse from(DiscodeitException exception) {
        ErrorCode errorCode = exception.getErrorCode();

        return new ErrorResponse(
                exception.getTimestamp(),
                errorCode.getErrorCode(),
                exception.getMessage(),
                exception.getDetails(),
                exception.getClass().getName(),
                errorCode.getStatus().value()
        );
    }

}
