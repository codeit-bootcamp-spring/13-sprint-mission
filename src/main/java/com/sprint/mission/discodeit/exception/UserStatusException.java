package com.sprint.mission.discodeit.exception;

public class UserStatusException extends DiscodeitException {
    public UserStatusException(String message) { super(ExceptionCode.SERVER_ERROR, message); }
    public UserStatusException(ExceptionCode code, String message) {
        super(code,message);
    }
    public UserStatusException(ExceptionCode code, String message, Throwable cause) { super(code, message, null, cause);}
}
