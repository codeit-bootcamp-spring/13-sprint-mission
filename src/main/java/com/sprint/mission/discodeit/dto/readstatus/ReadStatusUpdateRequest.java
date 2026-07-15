package com.sprint.mission.discodeit.dto.readstatus;

import com.sprint.mission.discodeit.dto.command.readstatus.ReadStatusCreateCommand;
import com.sprint.mission.discodeit.dto.command.readstatus.ReadStatusUpdateCommand;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record ReadStatusUpdateRequest(
        @Schema(description = "수정할 읽음 상태 정보")
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

    public ReadStatusUpdateCommand toCommand() {
        return new ReadStatusUpdateCommand(newLastReadAt);
    }
}
