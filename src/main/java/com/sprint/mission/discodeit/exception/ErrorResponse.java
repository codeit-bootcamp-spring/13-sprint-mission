package com.sprint.mission.discodeit.exception;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder(access = AccessLevel.PRIVATE)
public class ErrorResponse {
    private final Instant timestamp;
    private final String code;
    private final String message;
    private final Map<String, Object> details;
    private final String exceptionType;
    private final int status;

    public static ErrorResponse of(ErrorCode errorCode, Map<String, Object> details, String exceptionType) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .details(details)
                .exceptionType(exceptionType)
                .status(errorCode.getHttpStatus().value())
                .build();
    }
}
