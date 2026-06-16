package com.sprint.mission.discodeit.dto.readstatus;

import java.util.UUID;

public record ReadStatusCreateRequest(
        UUID userId,
        UUID channelId
)
{
    public ReadStatusCreateRequest {
        validate(userId, "사용자ID");
        validate(channelId, "채널ID");
    }

    private static void validate(UUID value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + "가 존재하지 않습니다.");
        }
    }
}
