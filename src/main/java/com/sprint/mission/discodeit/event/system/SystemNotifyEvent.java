package com.sprint.mission.discodeit.event.system;

public record SystemNotifyEvent(
        String job,
        String requestId,
        String exceptionMessage
) {
}
