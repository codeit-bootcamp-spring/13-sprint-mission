package com.sprint.mission.discodeit.exception;

public class MessageException extends DiscodeitException {
    public MessageException(String message) { super(ExceptionCode.SERVER_ERROR, message); }
    public MessageException(ExceptionCode code, String message) {
        super(code,message);
    }
    public MessageException(ExceptionCode code, String message, Throwable cause) { super(code, message, null, cause);}
}
