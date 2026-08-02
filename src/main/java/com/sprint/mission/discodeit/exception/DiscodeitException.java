package com.sprint.mission.discodeit.exception;

import java.util.Map;
import lombok.Getter;

@Getter
public class DiscodeitException extends RuntimeException {

    private final ErrorCode errorCode;

    private final Map<String, Object> details;

    public DiscodeitException(ErrorCode errorCode, Map<String, Object> details) {
        // 부모(RuntimeException)에게 에러 메시지 전달
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = details;
    }
}