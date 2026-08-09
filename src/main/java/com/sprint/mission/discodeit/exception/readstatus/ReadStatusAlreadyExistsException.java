package com.sprint.mission.discodeit.exception.readstatus;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class ReadStatusAlreadyExistsException extends ReadStatusException {

  public ReadStatusAlreadyExistsException(UUID userId, UUID channelId) {
    super(ErrorCode.DUPLICATE_READSTATUS, "ReadStatus with userId " + userId + " and channelId "
            + channelId + " already exists",
        Map.of("조회를 시도한 사용자의 ID 정보", userId, "조회를 시도한 채널의 ID 정보", channelId));
  }
}
