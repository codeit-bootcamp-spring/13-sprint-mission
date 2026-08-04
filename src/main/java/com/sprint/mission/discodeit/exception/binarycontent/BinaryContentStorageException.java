package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class BinaryContentStorageException
        extends BinaryContentException {

    public BinaryContentStorageException(
            UUID binaryContentId,
            String operation,
            Throwable cause
    ) {
        super(
                ErrorCode.BINARY_CONTENT_STORAGE_ERROR,
                Map.of(
                        "binaryContentId",
                        binaryContentId == null ? "unknown" : binaryContentId,
                        "operation",
                        operation
                ),
                cause
        );
    }
}