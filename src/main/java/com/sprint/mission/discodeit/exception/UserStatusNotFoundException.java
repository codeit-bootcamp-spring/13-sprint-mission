package com.sprint.mission.discodeit.exception;

import java.util.UUID;

public class UserStatusNotFoundException extends RuntimeException {

  public UserStatusNotFoundException(UUID id) {
    super("존재하지 않는 유저 상태 정보입니다.");
  }
}
