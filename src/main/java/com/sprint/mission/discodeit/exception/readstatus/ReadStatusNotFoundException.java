package com.sprint.mission.discodeit.exception.readstatus;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class ReadStatusNotFoundException extends ReadStatusException {

  public ReadStatusNotFoundException(UUID readStatusId) {
    super(ErrorCode.READSTATUS_NOT_FOUND, "ReadStatus with id " + readStatusId + " not found",
        Map.of("조회 시도한 읽기 상태의 ID 정보", readStatusId));
  }
}
