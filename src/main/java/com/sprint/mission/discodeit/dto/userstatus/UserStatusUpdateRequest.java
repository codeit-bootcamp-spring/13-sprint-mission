package com.sprint.mission.discodeit.dto.userstatus;

import java.time.Instant;

public record UserStatusUpdateRequest(
        Instant newLastActiveAt
)

{
    public UserStatusUpdateRequest {
        validate(newLastActiveAt, "마지막 접속 시간");
    }

    private static void  validate(Instant value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + "을 입력해주세요");
        }
    }
}
