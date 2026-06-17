package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.*;

import java.time.*;
import java.util.*;

public record ChannelResponse(
        UUID id,
        String name,
        String description,
        ChannelType type,
        Instant lastMessageAt,
        List<UUID> userIds
) {

    public static ChannelResponse from(
            Channel channel,
            Instant lastMessageAt,
            List<UUID> userIds
    ) {
        return new ChannelResponse(
                channel.getId(),
                channel.getName(),
                channel.getDescription(),
                channel.getType(),
                lastMessageAt,
                userIds
        );
    }

    @Override
    public String toString() {
        return """
            채널 정보
            ====================
            ID          : %s
            Name        : %s
            Description : %s
            Type        : %s
            LastMessage : %s
            Users       : %s
            ====================
            """
                .formatted(
                        id,
                        name,
                        description,
                        type,
                        lastMessageAt,
                        userIds
                );
    }
}