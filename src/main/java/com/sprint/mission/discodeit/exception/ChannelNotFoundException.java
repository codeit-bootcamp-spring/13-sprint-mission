package com.sprint.mission.discodeit.exception;

public class ChannelNotFoundException extends ChannelException {
    public ChannelNotFoundException(String format, Object... args) {
        super(ExceptionCode.RESOURCE_NOT_FOUNDED,String.format(format, args));
    }
}
