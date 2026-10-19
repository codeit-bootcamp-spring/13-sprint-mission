package com.sprint.mission.discodeit.event.message;

import com.sprint.mission.discodeit.dto.response.MessageDto;

import java.util.UUID;

public record MessageCreateEvent(
        MessageDto message
) {
}
