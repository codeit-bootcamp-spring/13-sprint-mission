package com.sprint.mission.discodeit.exception;

public class ChannelException extends DiscodeitException {
    public ChannelException(String message) { super(ExceptionCode.SERVER_ERROR, message); }
    public ChannelException(ExceptionCode code, String message) {
        super(code,message);
    }
    public ChannelException(ExceptionCode code, String message, Throwable cause) { super(code, message, null, cause);}
}