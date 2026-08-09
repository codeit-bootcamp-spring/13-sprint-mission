package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class BinaryContentNotFoundException extends BinaryContentException {

  public BinaryContentNotFoundException(UUID binaryContentId) {
    super(ErrorCode.CONTENT_NOT_FOUND, "BinaryContent with id " + binaryContentId + " not found",
        Map.of("조회 시도한 파일의 ID 정보", binaryContentId));
  }
}
