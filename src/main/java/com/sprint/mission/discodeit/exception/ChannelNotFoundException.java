package com.sprint.mission.discodeit.exception;

import java.util.UUID;

public class ChannelNotFoundException extends RuntimeException {

  public ChannelNotFoundException(UUID id) {
    super("채널을 찾을 수 없습니다. id: " + id);
  }
}
