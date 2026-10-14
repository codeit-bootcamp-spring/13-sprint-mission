package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ExceptionCode;

public class BinaryContentNotFoundException extends DiscodeitException {
    public BinaryContentNotFoundException(String contentId) {
        super(ExceptionCode.BINARY_CONTENT_NOT_FOUND,"BinaryContent - no file on id: {}", contentId);
    }
}
