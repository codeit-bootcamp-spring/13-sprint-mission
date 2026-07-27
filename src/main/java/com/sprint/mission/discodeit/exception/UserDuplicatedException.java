package com.sprint.mission.discodeit.exception;

public class UserDuplicatedException extends UserException {
    public UserDuplicatedException(String format, Object... args) {
        super(ExceptionCode.REQUEST_VALUE_ERROR,String.format(format, args));
    }
}
