package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ExceptionCode;

public class UserDuplicatedException extends DiscodeitException {
    public UserDuplicatedException(String format, Object... args) {
        super(ExceptionCode.REQUEST_VALUE_ERROR,String.format(format, args));
    }
}
