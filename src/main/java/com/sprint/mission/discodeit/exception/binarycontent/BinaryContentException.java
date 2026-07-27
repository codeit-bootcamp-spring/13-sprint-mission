package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.*;

import java.util.*;

public class BinaryContentException extends DiscodeitException {
    public BinaryContentException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BinaryContentException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
