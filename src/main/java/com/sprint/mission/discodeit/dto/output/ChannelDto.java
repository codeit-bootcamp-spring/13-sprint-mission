package com.sprint.mission.discodeit.dto.output;

import com.sprint.mission.discodeit.entity.ChannelType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@Builder
public class ChannelDto {
    private final UUID id;
    private final ChannelType type;
    private final String name;
    private final String description;
    private final List<UUID> participantIds;
    private final Instant lastMessageAt;

}
