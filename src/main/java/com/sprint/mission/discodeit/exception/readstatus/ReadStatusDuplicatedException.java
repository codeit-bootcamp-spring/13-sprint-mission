package com.sprint.mission.discodeit.exception.readstatus;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ExceptionCode;

public class ReadStatusDuplicatedException extends DiscodeitException {
    public ReadStatusDuplicatedException(String format, Object... args) {
        super(ExceptionCode.READ_STATUS_DUPLICATE_ERROR,String.format(format, args));
    }
}
