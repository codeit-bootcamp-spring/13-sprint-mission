package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class BinaryContentStorageException extends BinaryContentException {

    public BinaryContentStorageException(
            UUID binaryContentId,
            String fileName,
            Throwable cause
    ) {
        super(
                ErrorCode.FILE_UPLOAD_FAILED,
                Map.of(
                        "binaryContentId", binaryContentId,
                        "fileName", fileName
                )
        );

        initCause(cause);
    }
}