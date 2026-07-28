package com.sprint.mission.discodeit.dto.userstatus;

import com.sprint.mission.discodeit.dto.command.userstatus.UserStatusCreateCommand;
import com.sprint.mission.discodeit.dto.command.userstatus.UserStatusUpdateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record UserStatusUpdateRequest(
        @Schema(description = "변경할 User 온라인 상태 정보")
        @NotNull(message = "마지막 접속 시간을 입력해주세요.")
        Instant newLastActiveAt
)

{
    public UserStatusUpdateCommand toCommand() {
        return new UserStatusUpdateCommand(newLastActiveAt);
    }
}
