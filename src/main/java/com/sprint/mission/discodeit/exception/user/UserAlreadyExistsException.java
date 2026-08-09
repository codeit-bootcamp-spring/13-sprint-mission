package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;


// 이미 존재하는 이메일/사용자명으로 가입 시도할 때 던지는 예외
public class UserAlreadyExistsException extends UserException {

    public UserAlreadyExistsException(String field, String value) {
        super(
                // field가 "email"이면 EMAIL_DUPLICATE, "username"이면 USERNAME_DUPLICATE
                field.equals("email") ? ErrorCode.USER_EMAIL_DUPLICATE : ErrorCode.USER_USERNAME_DUPLICATE,
                Map.of(field, value)  // 어떤 값이 중복인지 기록
        );
    }
}