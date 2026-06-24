package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.Channel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ChannelResponse {

    private UUID id;
    private String name;
    private Channel.ChannelType type;
    private String description;
    private Instant latestMessageAt;
    private List<UUID> participantIds;

    public static ChannelResponse from(Channel channel) {
        return new ChannelResponse(
                channel.getId(),
                channel.getName(),
                channel.getType(),
                channel.getDescription(),
                null,
                List.of()
        );
    }

    public static ChannelResponse from(
            Channel channel,
            Instant latestMessageAt,
            List<UUID> participantIds) {
        return new ChannelResponse(
                channel.getId(),
                channel.getName(),
                channel.getType(),
                channel.getDescription(),
                latestMessageAt,
                participantIds
        );
    }

}
