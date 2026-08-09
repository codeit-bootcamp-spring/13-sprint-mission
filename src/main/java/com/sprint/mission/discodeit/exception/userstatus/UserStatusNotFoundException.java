package com.sprint.mission.discodeit.exception.userstatus;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class UserStatusNotFoundException extends UserStatusException {

  public UserStatusNotFoundException(String field, Object value) {
    super(ErrorCode.USERSTATUS_NOT_FOUND, "UserStatus with " + field + " " + value + " not found",
        Map.of(field, value));
  }
}
