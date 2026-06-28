package com.sprint.mission.discodeit.dto.readstatus;

import java.time.Instant;

public record ReadStatusUpdateRequest(

        Instant newLastReadAt
)
{
    public ReadStatusUpdateRequest {
        validate(newLastReadAt, "마지막 읽은 시간");
    }

    private static void  validate(Instant validate, String fieldName) {
        if (validate == null) {
            throw new IllegalArgumentException(fieldName + "입력해 주세요.");
        }
    }
}
