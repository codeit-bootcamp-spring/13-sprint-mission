package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.ErrorCode;

import java.util.Map;

public class ChannelAlreadyExistsException extends ChannelException {
    public ChannelAlreadyExistsException(Map<String, Object> details) {
        super(ErrorCode.DUPLICATE_CHANNEL, details);
    }

    public static ChannelAlreadyExistsException withName(String channelName) {
        return new ChannelAlreadyExistsException(Map.of("channelName", channelName));
    }
}
