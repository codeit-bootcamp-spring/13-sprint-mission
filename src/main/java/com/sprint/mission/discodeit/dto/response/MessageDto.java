package com.sprint.mission.discodeit.dto.response;

import java.time.*;
import java.util.*;

public record MessageDto(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        String content,
        UUID channelId,
        UserDto author,
        List<BinaryContentDto> attachments
) {

}
