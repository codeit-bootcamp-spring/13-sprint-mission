package com.sprint.mission.discodeit.exception.file;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class FileStorageException extends DiscodeitException {
    public FileStorageException(String fileName) {
        super(ErrorCode.FILE_STORAGE_FAILED, Map.of("fileName", fileName));
    }

    public FileStorageException(UUID binaryContentId) {
        super(ErrorCode.FILE_STORAGE_FAILED, Map.of("binaryContentId", binaryContentId));
    }
}
