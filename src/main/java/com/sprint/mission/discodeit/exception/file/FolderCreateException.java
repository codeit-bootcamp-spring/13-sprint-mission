package com.sprint.mission.discodeit.exception.file;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;

import java.util.Map;

public class FolderCreateException extends DiscodeitException {
    public FolderCreateException(String path) {
        super(ErrorCode.FOLDER_CREATE_FAILED, Map.of("path", path));
    }
}
