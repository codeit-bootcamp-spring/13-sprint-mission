package com.sprint.mission.discodeit.exception;

public class UserNotFoundException extends UserException {
    public UserNotFoundException(String format, Object... args) {
        super(ExceptionCode.RESOURCE_NOT_FOUNDED,String.format(format, args));
    }
}
