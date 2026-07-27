package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.*;

import java.util.*;

public class ChannelNotFoundException extends ChannelException {

    public ChannelNotFoundException(UUID channelId) {
        super(ErrorCode.CHANNEL_NOT_FOUND, Map.of("channelId", channelId));
    }
}
