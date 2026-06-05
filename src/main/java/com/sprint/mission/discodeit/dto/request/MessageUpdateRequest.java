package com.sprint.mission.discodeit.dto.request;

import java.util.List;
import java.util.UUID;

public record MessageUpdateRequest(
        UUID messageId,
        String content,
        List<String> attachmentPathList
) {
}
