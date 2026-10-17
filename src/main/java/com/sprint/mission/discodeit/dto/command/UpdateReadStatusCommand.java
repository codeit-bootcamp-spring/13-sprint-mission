package com.sprint.mission.discodeit.dto.command;

import java.time.Instant;

public record UpdateReadStatusCommand(
        Instant lastReadTime,
        Boolean newNotificationEnabled
) {

}