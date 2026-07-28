package com.sprint.mission.discodeit.dto.userstatus;

import com.sprint.mission.discodeit.dto.command.userstatus.UserStatusCreateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UserStatusCreateRequest(
        @Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000",  requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "사용자 ID가 존재하지 않습니다.")
        UUID userId

)
{
    public UserStatusCreateCommand toCommand() {
        return new UserStatusCreateCommand(userId);
    }

}
