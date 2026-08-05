package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class BinaryContentDownloadException extends BinaryContentException {

  public BinaryContentDownloadException(UUID binaryContentId, Throwable cause) {
    super(ErrorCode.BINARY_CONTENT_DOWNLOAD_FAILED,
        Map.of("binaryContentId", binaryContentId), cause);
  }
}
