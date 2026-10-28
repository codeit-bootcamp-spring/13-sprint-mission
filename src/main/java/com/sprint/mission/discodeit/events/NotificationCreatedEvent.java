package com.sprint.mission.discodeit.events;

import com.sprint.mission.discodeit.dto.response.NotificationDto;

/**
 * 새로 저장한 알림의 DTO를 전달하여 수신자에게 notifications.created를 발행하게 한다.
 */
public record NotificationCreatedEvent(NotificationDto notification) {
}
