package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;

public record UserCreateRequest(
        String userName,
        String email,
        String password,
        BinaryContentCreateRequest profile
) {
}
