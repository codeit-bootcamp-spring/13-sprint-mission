package com.sprint.mission.discodeit.dto.channel;

import java.util.List;
import java.util.UUID;

public record PrivateChannelRequest(
        List<UUID> participantIds
)
{
    public PrivateChannelRequest{
        if (participantIds == null || participantIds.size() < 2) {
            throw new IllegalArgumentException("비공개 채널은 최소 2명이 필요합니다.");
        }
    }

}
