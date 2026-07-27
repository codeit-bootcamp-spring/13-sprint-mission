package com.sprint.mission.discodeit.exception.message;

import com.sprint.mission.discodeit.exception.*;

import java.util.*;

public class MessageNotFoundException extends MessageException {
    public MessageNotFoundException(UUID messageId) {
        super(ErrorCode.MESSAGE_NOT_FOUND, Map.of("messageId", messageId));
    }
}
