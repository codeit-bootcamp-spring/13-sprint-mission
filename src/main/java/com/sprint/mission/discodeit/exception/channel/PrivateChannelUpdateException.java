package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class PrivateChannelUpdateException extends ChannelException {

  public PrivateChannelUpdateException(UUID channelId) {
    super(ErrorCode.PRIVATE_CHANNEL_UPDATE, "Private channel cannot be updated",
        Map.of("업데이트 시도한 PRIVATE 채널의 ID", channelId));
  }
}
