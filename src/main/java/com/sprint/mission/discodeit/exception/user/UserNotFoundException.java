package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ExceptionCode;

public class UserNotFoundException extends DiscodeitException {
    public UserNotFoundException(){
        super(ExceptionCode.RESOURCE_NOT_FOUNDED);
    }

    public UserNotFoundException(String format, Object... args) {
        super(ExceptionCode.RESOURCE_NOT_FOUNDED,String.format(format, args));
    }
}
