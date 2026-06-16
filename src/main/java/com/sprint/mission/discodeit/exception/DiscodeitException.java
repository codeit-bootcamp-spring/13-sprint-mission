package com.sprint.mission.discodeit.exception;

import lombok.Getter;

@Getter
public class DiscodeitException extends RuntimeException {
    private final String type;
    private final Integer code;
    public DiscodeitException(
            String message,
            Throwable cause,
            String type,
            Integer code
//            StackTraceElement[] stackTrace
    ) {
        super(message,cause);
        this.type = type;
        this.code = code;
    }

    public DiscodeitException(
            String message,
            String type,
            Integer code
    ){
        super(message);
        this.type = type;
        this.code = code;
    }
}
