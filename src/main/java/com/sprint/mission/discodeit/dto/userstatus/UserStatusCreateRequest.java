package com.sprint.mission.discodeit.dto.userstatus;

import java.util.UUID;

public record UserStatusCreateRequest(
        UUID userId

)
{
    public UserStatusCreateRequest {
        validate(userId, "ID");
    }

    private static void validate(UUID value, String fieldName) {
        if (value == null){
            throw new IllegalArgumentException(fieldName + " 가 존재하지 않습니다.");
        }
    }

}
