package com.sprint.mission.discodeit.exception;

public class DiscodeitChannelException extends DiscodeitException {
    public DiscodeitChannelException(
            String m,
            Throwable cause,
            Integer code
    ) {
        super(m, cause, "User",code);
    }
    public DiscodeitChannelException(
            String m,
            Integer code
    ){
        super(m, "User", code);
    }
}
