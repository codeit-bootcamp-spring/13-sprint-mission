package com.sprint.mission.discodeit.exception;

import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
public class ErrorResponse {
    private final Instant timestamp;
    private final String code;
    private final String message;
    private final Map<String, Object> details;
    private final String exceptionType;
    private final int status;

    public ErrorResponse(ErrorCode errorCode, Map<String, Object> details, String exceptionType) {
        this.timestamp = Instant.now();
        this.code = errorCode.name();
        this.message = errorCode.getMessage();
        this.details = details;
        this.exceptionType = exceptionType;
        this.status = errorCode.getHttpStatus().value();
    }

}
