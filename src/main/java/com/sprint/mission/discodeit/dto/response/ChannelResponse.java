package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.Channel;

import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChannelResponse(
    UUID id,
    Instant createdAt,
    Instant updatedAt,
    Channel.ChannelType type,
    String name,
    String description
) {


  public static ChannelResponse from(Channel channel) {
    return new ChannelResponse(
        channel.getId(),
        channel.getCreatedAt(),
        channel.getUpdatedAt(),
        channel.getType(),
        channel.getName(),
        channel.getDescription()
    );
  }

}
