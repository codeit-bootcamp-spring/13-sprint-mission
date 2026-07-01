package com.sprint.mission.discodeit.dto.userstatus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record UserStatusUpdateRequest(
        @Schema(description = "변경할 User 온라인 상태 정보")
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
