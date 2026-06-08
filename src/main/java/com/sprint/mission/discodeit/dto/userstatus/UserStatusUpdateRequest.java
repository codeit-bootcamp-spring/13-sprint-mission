package com.sprint.mission.discodeit.dto.userstatus;

import java.time.Instant;

public record UserStatusUpdateRequest(
        Instant lastAccessedAt
)

{
    public UserStatusUpdateRequest {
        validate(lastAccessedAt, "userStatus");
    }

    private static void  validate(Instant value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + "을 입력해주세요");
        }
    }
}
