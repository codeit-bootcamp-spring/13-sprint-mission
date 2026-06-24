package com.sprint.mission.discodeit.dto.response;

public record ErrorResponse(
        String errorType,
        String errorDescription,
        String message
) {}
