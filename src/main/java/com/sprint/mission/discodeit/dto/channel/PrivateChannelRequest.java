package com.sprint.mission.discodeit.dto.channel;

import com.sprint.mission.discodeit.dto.command.channel.PrivateChannelCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record PrivateChannelRequest(

        @Schema(description = "비공개 채널 참가자 ID 몰록",
                example = "[\"550e8400-e29b-41d4-a716-446655440000\"]",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @Size(min = 2, message = "비공개 채널은 최소 2명이 필요합니다.")
        @NotNull(message = "참가자 목록은 필수입니다.")
        List<UUID> participantIds
)
{
    public PrivateChannelCommand toCommand() {
        return new PrivateChannelCommand(participantIds);
    }
}
