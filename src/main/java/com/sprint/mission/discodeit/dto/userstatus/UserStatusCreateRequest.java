package com.sprint.mission.discodeit.dto.userstatus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record UserStatusCreateRequest(
        @Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000",  requiredMode = Schema.RequiredMode.REQUIRED)
        UUID userId

)
{
    public UserStatusCreateRequest {
        validate(userId, "사용자 ID");
    }

    private static void validate(UUID value, String fieldName) {
        if (value == null){
            throw new IllegalArgumentException(fieldName + " 가 존재하지 않습니다.");
        }
    }

}
