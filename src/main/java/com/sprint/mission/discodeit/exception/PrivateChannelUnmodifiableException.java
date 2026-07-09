package com.sprint.mission.discodeit.exception;

public class PrivateChannelUnmodifiableException extends RuntimeException {

  public PrivateChannelUnmodifiableException() {
    super("프라이빗 채널은 수정할 수 없습니다.");
  }
}
