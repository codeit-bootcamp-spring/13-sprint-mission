package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.*;

import java.util.*;

public class PrivateChannelUpdateException extends ChannelException {

    public PrivateChannelUpdateException(UUID channelId) {
        super(ErrorCode.PRIVATE_CHANNEL_UPDATE, Map.of("channelId", channelId));

    }
}
