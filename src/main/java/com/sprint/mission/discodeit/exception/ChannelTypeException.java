package com.sprint.mission.discodeit.exception;

public class ChannelTypeException extends ChannelException {
    public ChannelTypeException(String format, Object... args) {
        super(ExceptionCode.RESOURCE_NOT_FOUNDED,String.format(format, args));
    }
}
