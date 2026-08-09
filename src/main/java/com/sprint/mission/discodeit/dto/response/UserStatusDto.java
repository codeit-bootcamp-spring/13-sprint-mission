package com.sprint.mission.discodeit.dto.response;

import java.time.*;
import java.util.*;

public record UserStatusDto(
        UUID id,
        UUID userId,
        Instant lastActiveAt
) {

}
