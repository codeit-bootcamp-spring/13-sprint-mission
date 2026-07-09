package com.sprint.mission.discodeit.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(UUID id) {
    super("사용자를 찾을 수 없습니다. id: " + id);
  }

  public UserNotFoundException(String username) {
    super("사용자 이름을 찾을 수 없습니다. username: " + username);
  }
}
