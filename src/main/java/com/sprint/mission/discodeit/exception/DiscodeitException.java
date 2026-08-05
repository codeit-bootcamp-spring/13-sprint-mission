package com.sprint.mission.discodeit.exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DiscodeitException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    public DiscodeitException(ErrorCode errorCode) {
        this(errorCode, Collections.emptyMap());
    }

    public DiscodeitException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = Collections.unmodifiableMap(new LinkedHashMap<>(details));
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
