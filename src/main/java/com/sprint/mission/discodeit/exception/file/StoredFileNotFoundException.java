package com.sprint.mission.discodeit.exception.file;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class StoredFileNotFoundException extends DiscodeitException {
    public StoredFileNotFoundException(UUID binaryContentId) {
        super(ErrorCode.STORED_FILE_NOT_FOUND, Map.of("binaryContentId", binaryContentId));
    }
}
