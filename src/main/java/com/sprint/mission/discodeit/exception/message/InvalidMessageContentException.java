package com.sprint.mission.discodeit.exception.message;

import com.sprint.mission.discodeit.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class InvalidMessageContentException extends MessageException {

    public InvalidMessageContentException(UUID channelId, UUID userId) {
        super(ErrorCode.INVALID_MESSAGE_CONTENT, Map.of(
                "channelId", channelId,
                "userId", userId
        ));
    }
}