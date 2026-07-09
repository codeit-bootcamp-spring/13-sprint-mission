package com.sprint.mission.discodeit.exception;

import java.util.UUID;

public class ReadStatusNotFoundException extends RuntimeException {

  public ReadStatusNotFoundException(UUID id) {
    super("읽음 상태를 찾을 수 없습니다.");
  }
}
