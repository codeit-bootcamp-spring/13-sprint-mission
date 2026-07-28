package com.sprint.mission.discodeit.dto.readstatus;

import com.sprint.mission.discodeit.dto.command.readstatus.ReadStatusUpdateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ReadStatusUpdateRequest(
        @Schema(description = "수정할 읽음 상태 정보")
        @NotNull(message = "마지막 읽은 시간을 입력해 주세요.")
        Instant newLastReadAt
)
{
    public ReadStatusUpdateCommand toCommand() {
        return new ReadStatusUpdateCommand(newLastReadAt);
    }
}
