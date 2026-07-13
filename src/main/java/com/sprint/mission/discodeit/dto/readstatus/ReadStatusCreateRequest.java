package com.sprint.mission.discodeit.dto.readstatus;

import com.sprint.mission.discodeit.dto.command.readstatus.ReadStatusCreateCommand;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;
@Schema(description = "Message 읽음 상태 생성 정보")
public record ReadStatusCreateRequest(
        @Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000",  requiredMode = Schema.RequiredMode.REQUIRED)
        UUID userId,
        @Schema(description = "채널 ID", example = "383b89f5-3200-4504-9283-2d46cd007444",  requiredMode = Schema.RequiredMode.REQUIRED)
        UUID channelId
)
{
    public ReadStatusCreateRequest {
        validate(userId, "사용자 ID");
        validate(channelId, "채널 ID");
    }

    private static void validate(UUID value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + "가 존재하지 않습니다.");
        }
    }

    public ReadStatusCreateCommand toCommand() {
        return new ReadStatusCreateCommand(userId, channelId);
    }
}
