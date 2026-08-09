package com.sprint.mission.discodeit.exception;

public class NoChangesException extends DiscodeitException {

  public NoChangesException() {
    super(ErrorCode.NO_CHANGES, "변경사항이 없습니다!", null);
  }
}
