package com.sprint.mission.discodeit.exception;

import java.util.Map;

public class DuplicateUserException extends UserException {

  public DuplicateUserException(String field, String value) {
    super(ErrorCode.DUPLICATE_USER, Map.of("field", field, "value", value));
  }
}
