package com.sprint.mission.discodeit.exception;

public class UserException extends DiscodeitException {
    public UserException(String message) { super(ExceptionCode.SERVER_ERROR, message); }
    public UserException(ExceptionCode code, String message) {
        super(code,message);
    }
    public UserException(ExceptionCode code, String message, Throwable cause) { super(code, message, null, cause);}
}
