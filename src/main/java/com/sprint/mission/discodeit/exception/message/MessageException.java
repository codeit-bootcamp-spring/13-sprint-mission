package com.sprint.mission.discodeit.exception.message;

import com.sprint.mission.discodeit.exception.*;

import java.util.*;

public class MessageException extends DiscodeitException {

    public MessageException(ErrorCode errorCode) {
        super(errorCode);
    }

    public MessageException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
