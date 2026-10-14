package com.sprint.mission.discodeit.event.binarycontent;

import java.util.UUID;

public record BinaryContentStorageFailedEvent(
    String operation, String requestId, UUID binaryContentId, String reason) {
}
