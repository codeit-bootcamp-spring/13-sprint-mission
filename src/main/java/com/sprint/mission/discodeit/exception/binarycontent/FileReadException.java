package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.ErrorCode;

import java.util.Map;

public class FileReadException extends BinaryContentException {
    public FileReadException(Map<String, Object> details) {
        super(ErrorCode.FILE_READ_FAILED, details);
    }

    public static FileReadException withFileName(String fileName) {
        return new FileReadException(Map.of("fileName", fileName));
    }
}
