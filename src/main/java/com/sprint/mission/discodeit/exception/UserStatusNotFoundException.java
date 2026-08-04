package com.sprint.mission.discodeit.exception;

public class UserStatusNotFoundException extends UserStatusException {
    public UserStatusNotFoundException(String message) {
        super(ExceptionCode.RESOURCE_NOT_FOUNDED,message);
    }
}
