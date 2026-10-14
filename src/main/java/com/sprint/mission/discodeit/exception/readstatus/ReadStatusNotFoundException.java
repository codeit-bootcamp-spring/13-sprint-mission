package com.sprint.mission.discodeit.exception.readstatus;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ExceptionCode;

public class ReadStatusNotFoundException extends DiscodeitException {
    public ReadStatusNotFoundException(String format, Object... args) {
        super(ExceptionCode.READ_STATUS_NOT_FOUND_ERROR,String.format(format, args));
    }
}
