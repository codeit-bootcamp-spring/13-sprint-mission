package com.sprint.mission.discodeit.exception.readstatus;

import com.sprint.mission.discodeit.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class ReadStatusAlreadyExistException extends ReadStatusException {

    public ReadStatusAlreadyExistException(UUID channelId, UUID userId) {
        super(ErrorCode.READ_STATUS_ALREADY_EXISTS, Map.of(
                "channelId", channelId,
                "userId", userId
        ));
    }
}