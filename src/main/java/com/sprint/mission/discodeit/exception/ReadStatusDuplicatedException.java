package com.sprint.mission.discodeit.exception;

public class ReadStatusDuplicatedException extends ReadStatusException {
    public ReadStatusDuplicatedException(String format, Object... args) {
        super(ExceptionCode.REQUEST_VALUE_ERROR,String.format(format, args));
    }
}
