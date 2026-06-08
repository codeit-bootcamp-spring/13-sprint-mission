package com.sprint.mission.discodeit.dto.readstatus;

import java.time.Instant;

public record ReadStatusUpdateRequest(

        Instant lastReadAt
)
{
    public ReadStatusUpdateRequest {
        validate(lastReadAt, "ReadStatus");
    }

    private static void  validate(Instant validate, String fieldName) {
        if (validate == null) {
            throw new IllegalArgumentException(fieldName + "입력해 주세요.");
        }
    }
}
