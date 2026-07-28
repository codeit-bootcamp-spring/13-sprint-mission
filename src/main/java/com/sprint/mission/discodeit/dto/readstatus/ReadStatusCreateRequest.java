package com.sprint.mission.discodeit.dto.readstatus;

import com.sprint.mission.discodeit.dto.command.readstatus.ReadStatusCreateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;
@Schema(description = "Message 읽음 상태 생성 정보")
public record ReadStatusCreateRequest(
        @Schema(description = "사용자 id", example = "550e8400-e29b-41d4-a716-446655440000",  requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "사용자 ID는 필수입니다.")
        UUID userId,
        @Schema(description = "채널 id", example = "383b89f5-3200-4504-9283-2d46cd007444",  requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "채널 ID는 필수 입니다.")
        UUID channelId
)
{
    public ReadStatusCreateCommand toCommand() {
        return new ReadStatusCreateCommand(userId, channelId);
    }
}
