package com.sprint.mission.discodeit.exception;

public class MessageNotFoundException extends MessageException {
    public MessageNotFoundException(String format, Object... args) {
        super(ExceptionCode.RESOURCE_NOT_FOUNDED,String.format(format, args));
    }
}
