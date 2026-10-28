package com.sprint.mission.discodeit.sse;

import java.util.UUID;

public record UserSseDto(
        UUID id,
        String username
) {
}
