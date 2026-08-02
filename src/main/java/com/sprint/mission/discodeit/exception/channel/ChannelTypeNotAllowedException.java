package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

/*
  ChannelTypeNotAllowedException: PRIVATE 채널을 수정하려 할 때 던지는 예외
*/
public class ChannelTypeNotAllowedException extends ChannelException {

    public ChannelTypeNotAllowedException(UUID channelId) {
        super(
                ErrorCode.CHANNEL_UPDATE_NOT_ALLOWED,
                Map.of("channelId", channelId)
        );
    }
}