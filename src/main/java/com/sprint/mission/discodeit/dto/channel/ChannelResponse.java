package com.sprint.mission.discodeit.dto.channel;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChannelResponse(
        UUID id,
        ChannelType type,
        String name,
        String description,
        Instant lastMessageAt, // 해당 채널의 가장 최근 메시지 정보 포함
        List<UUID> userIds
) {
    public static ChannelResponse from(Channel channel, Instant lastMessageAt, List<UUID> userIds) {
        return new ChannelResponse(
                channel.getId(),
                channel.getType(),
                channel.getName(),
                channel.getDescription(),
                lastMessageAt,
                userIds  // PRIVATE 채널인 경우, 참여한 user의 id 정보 포함
        );
    }
}
