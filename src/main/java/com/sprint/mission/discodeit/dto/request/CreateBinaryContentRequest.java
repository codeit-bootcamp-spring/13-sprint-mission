package com.sprint.mission.discodeit.dto.request;

import java.util.*;

public record CreateBinaryContentRequest(
        UUID userId,
        UUID messageId,
        String fileName,
        String contentType,
        byte[] data
) {
}
