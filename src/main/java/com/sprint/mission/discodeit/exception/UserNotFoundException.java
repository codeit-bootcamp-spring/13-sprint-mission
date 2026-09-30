package com.sprint.mission.discodeit.exception;

public class UserNotFoundException extends UserException {
    public UserNotFoundException(){
        super(ExceptionCode.RESOURCE_NOT_FOUNDED,"유저가 존재하지 않습니다.");
    }

    public UserNotFoundException(String format, Object... args) {
        super(ExceptionCode.RESOURCE_NOT_FOUNDED,String.format(format, args));
    }
}
