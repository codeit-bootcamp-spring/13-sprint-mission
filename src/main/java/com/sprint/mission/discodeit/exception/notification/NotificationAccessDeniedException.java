package com.sprint.mission.discodeit.exception.notification;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class NotificationAccessDeniedException extends DiscodeitException {
  public NotificationAccessDeniedException(UUID notificationId) {
    super(ErrorCode.NOTIFICATION_ACCESS_DENIED, Map.of("notificationId", notificationId));
  }
}
