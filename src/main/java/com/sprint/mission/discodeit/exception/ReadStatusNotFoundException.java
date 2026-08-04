package com.sprint.mission.discodeit.exception;

public class ReadStatusNotFoundException extends ReadStatusException {
    public ReadStatusNotFoundException(String format, Object... args) {
        super(ExceptionCode.RESOURCE_NOT_FOUNDED,String.format(format, args));
    }
}
