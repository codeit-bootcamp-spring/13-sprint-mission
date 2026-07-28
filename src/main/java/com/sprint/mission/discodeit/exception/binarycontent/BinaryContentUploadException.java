package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.ErrorCode;

public class BinaryContentUploadException extends BinaryContentException {

  public BinaryContentUploadException() {
    super(ErrorCode.CONTENT_UPLOAD_FAILED, "첨부 파일 업로드 실패", null);
  }
}
