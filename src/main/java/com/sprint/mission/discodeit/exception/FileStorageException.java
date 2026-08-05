package com.sprint.mission.discodeit.exception;

public class FileStorageException extends DiscodeitException {
    public FileStorageException(String message) {
        super(ExceptionCode.FILE_SYSTEM_ERROR,message);
    }
}
