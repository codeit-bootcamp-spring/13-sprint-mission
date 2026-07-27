package com.sprint.mission.discodeit.exception.storage;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class StorageException extends DiscodeitException {
    public StorageException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public static StorageException putFailed(UUID id) {
        return new StorageException(ErrorCode.STORAGE_PUT_FAILED, Map.of("id", id));
    }

    public static StorageException getFailed(UUID id) {
        return new StorageException(ErrorCode.STORAGE_GET_FAILED, Map.of("id", id));
    }

    public static StorageException initFailed(String path) {
        return new StorageException(ErrorCode.STORAGE_INIT_FAILED, Map.of("path", path));
    }
}
