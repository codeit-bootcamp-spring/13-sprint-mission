package com.sprint.mission.discodeit.exception;

public class DiscodeitUserException extends DiscodeitException {
    public DiscodeitUserException(
            String m,
            Throwable cause,
            Integer code
    ) {
        super(m, cause, "User",code);
    }
    public DiscodeitUserException(
            String m,
            Integer code
    ){
        super(m, "User", code);
    }
}
