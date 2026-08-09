package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.*;

import java.util.*;

public class ChannelException extends DiscodeitException {
    public ChannelException(ErrorCode erroeCode) {
        super(erroeCode);
    }

    public ChannelException(ErrorCode erroeCode, Map<String, Object> details) {
        super(erroeCode, details);
    }
}
