package com.sprint.mission.discodeit.exception.message;

import com.sprint.mission.discodeit.exception.ErrorCode;

import java.util.Map;

public class InvalidMessageException extends MessageException {

    public InvalidMessageException(String reason) {
        super(
                ErrorCode.INVALID_MESSAGE,
                Map.of("reason", reason)
        );
    }

    public InvalidMessageException(
            ErrorCode errorCode,
            String reason
    ) {
        super(
                errorCode,
                Map.of("reason", reason)
        );
    }
}