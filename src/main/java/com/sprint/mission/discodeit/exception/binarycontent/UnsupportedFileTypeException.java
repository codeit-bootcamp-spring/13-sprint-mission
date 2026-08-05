package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.ErrorCode;

import java.util.Map;

public class UnsupportedFileTypeException extends BinaryContentException {
    public UnsupportedFileTypeException(Map<String, Object> details) {
        super(ErrorCode.UNSUPPORTED_FILE_TYPE, details);
    }

    public static UnsupportedFileTypeException withType(String contentType) {
        return new UnsupportedFileTypeException(Map.of("contentType", contentType));
    }
}
