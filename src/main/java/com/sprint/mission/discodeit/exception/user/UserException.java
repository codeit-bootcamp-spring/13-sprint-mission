package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.*;

import java.util.*;

public class UserException extends DiscodeitException {

    public UserException (ErrorCode errorCode) {
        super(errorCode);
    }
    public UserException (ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
