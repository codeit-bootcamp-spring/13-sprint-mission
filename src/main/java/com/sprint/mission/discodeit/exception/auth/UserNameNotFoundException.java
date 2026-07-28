package com.sprint.mission.discodeit.exception.auth;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class UserNameNotFoundException extends AuthException {

  public UserNameNotFoundException(String username) {
    super(ErrorCode.USER_NOT_FOUND, "User with username" + username
        + " not found", Map.of("조회 시도한 사용자의 이름 정보", username));
  }
}
