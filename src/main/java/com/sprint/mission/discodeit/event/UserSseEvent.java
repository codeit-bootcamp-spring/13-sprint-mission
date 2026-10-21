package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.sse.UserSseDto;

public record UserSseEvent(
        String eventName,
        UserSseDto user
) {
}