package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.MessageDto;
import java.util.UUID;

public record MessageCreatedEvent(
    UUID channelId,
    UUID authorId,
    String authorName,
    String channelName,
    String content,
    MessageDto messageDto
) {

}