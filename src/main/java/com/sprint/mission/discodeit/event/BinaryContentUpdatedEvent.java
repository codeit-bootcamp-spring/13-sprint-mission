package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;

public record BinaryContentUpdatedEvent(
        BinaryContentDto binaryContent
) {
}
