package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class BinaryContentUploadException extends BinaryContentException {

  public BinaryContentUploadException(String fileName, Throwable cause) {
    super(ErrorCode.BINARY_CONTENT_UPLOAD_FAILED,
        Map.of("fileName", safeFileName(fileName)),
        cause
    );
  }

  public BinaryContentUploadException(UUID binaryContentId, Throwable cause) {
    super(ErrorCode.BINARY_CONTENT_UPLOAD_FAILED,
        Map.of("binaryContentId", binaryContentId),
        cause
    );
  }

  private static String safeFileName(String fileName) {
    return fileName == null ? "unknown" : fileName;
  }
}
