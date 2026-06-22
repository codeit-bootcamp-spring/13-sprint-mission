package com.sprint.mission.discodeit.exception;

import java.util.UUID;

public class ReadStatusAlreadyExistsException extends RuntimeException {

  public ReadStatusAlreadyExistsException(UUID userId, UUID channelId) {
    super("이미 읽음 상태가 존재합니다. (User: " + userId + ", Channel: " + channelId + ")");
  }
}
