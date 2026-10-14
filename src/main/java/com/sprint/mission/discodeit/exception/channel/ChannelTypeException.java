package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ExceptionCode;

public class ChannelTypeException extends DiscodeitException {
    public ChannelTypeException(String format, Object... args) {
        super(ExceptionCode.RESOURCE_NOT_FOUNDED,String.format(format, args));
    }
}
