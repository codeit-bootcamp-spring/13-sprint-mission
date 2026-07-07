package com.sprint.mission.discodeit.dto.request;

import java.util.*;

public record CreateBinaryContentRequest(
        String fileName,
        String contentType,
        byte[] bytes
) {
}
