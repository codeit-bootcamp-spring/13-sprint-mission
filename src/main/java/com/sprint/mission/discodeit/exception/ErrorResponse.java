package com.sprint.mission.discodeit.exception;

public record ErrorResponse(
        String errorType,
        String errorDescription,
        String message
) {}
