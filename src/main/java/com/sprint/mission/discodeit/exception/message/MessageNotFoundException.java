package com.sprint.mission.discodeit.exception.message;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ExceptionCode;

public class MessageNotFoundException extends DiscodeitException {
    public MessageNotFoundException(){
        super(ExceptionCode.MESSAGE_NOT_FOUND);
    }

    public MessageNotFoundException(String format, Object... args) {
        super(ExceptionCode.MESSAGE_NOT_FOUND,String.format(format, args));
    }
}
