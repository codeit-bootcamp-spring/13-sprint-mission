package com.sprint.mission.discodeit.exception.userstatus;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class UserStatusAlreadyExistsException extends UserStatusException {

  public UserStatusAlreadyExistsException(UUID userId) {
    super(ErrorCode.DUPLICATE_USERSTATUS, "UserStatus with id " + userId + " already exists",
        Map.of("조회 시도한 사용자 상태의 ID 정보", userId));
  }
}
