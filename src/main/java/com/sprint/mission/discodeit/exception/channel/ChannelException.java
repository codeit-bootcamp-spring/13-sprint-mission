package com.sprint.mission.discodeit.exception.channel;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class ChannelException extends
    DiscodeitException { // 실제로 활용되는 클래스 < 예외 클래스 계층 구조 명확하게 하기 위한 클래스

  public ChannelException(ErrorCode errorCode, String message, Map<String, Object> details) {
    super(errorCode, message, details);
  }
}
