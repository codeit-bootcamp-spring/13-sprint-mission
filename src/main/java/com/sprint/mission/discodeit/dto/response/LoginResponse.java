package com.sprint.mission.discodeit.dto.response;

import java.util.*;

public record LoginResponse(
        UUID Id,
        String username,
        String email

) {
}
