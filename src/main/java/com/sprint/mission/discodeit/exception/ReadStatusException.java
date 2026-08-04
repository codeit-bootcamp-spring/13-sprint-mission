package com.sprint.mission.discodeit.exception;

public class ReadStatusException extends DiscodeitException {
    public ReadStatusException(String message) { super(ExceptionCode.SERVER_ERROR, message); }
    public ReadStatusException(ExceptionCode code, String message) {
        super(code,message);
    }
    public ReadStatusException(ExceptionCode code, String message, Throwable cause) { super(code, message, null, cause);}
}
