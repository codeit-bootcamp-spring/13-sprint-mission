package com.sprint.mission.discodeit.dto.response;

import java.time.Instant;
import java.util.UUID;

public record UserDto(
    UUID id, String username,
    String email, BinaryContentDto profile, Boolean online
) {

}
