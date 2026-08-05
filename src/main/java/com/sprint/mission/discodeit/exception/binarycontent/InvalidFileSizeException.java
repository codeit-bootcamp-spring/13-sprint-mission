package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.ErrorCode;

import java.util.Map;

public class InvalidFileSizeException extends BinaryContentException {
    public InvalidFileSizeException(Map<String, Object> details) {
        super(ErrorCode.INVALID_FILE_SIZE, details);
    }

    public static InvalidFileSizeException withSize(long fileSize){
        return new InvalidFileSizeException(Map.of("fileSize", fileSize));
    }
}
