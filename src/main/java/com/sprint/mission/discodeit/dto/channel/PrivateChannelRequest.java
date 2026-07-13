package com.sprint.mission.discodeit.dto.channel;

import com.sprint.mission.discodeit.dto.command.channel.PrivateChannelCommand;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

public record PrivateChannelRequest(

        @Schema(description = "비공개 채널 참가자 ID 몰록",
                example = "[\"550e8400-e29b-41d4-a716-446655440000\"]",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<UUID> participantIds
)
{
    public PrivateChannelRequest{
        if (participantIds == null || participantIds.size() < 2) {
            throw new IllegalArgumentException("비공개 채널은 최소 2명이 필요합니다.");
        }
    }

    public PrivateChannelCommand toCommand() {
        return new PrivateChannelCommand(participantIds);
    }

}
