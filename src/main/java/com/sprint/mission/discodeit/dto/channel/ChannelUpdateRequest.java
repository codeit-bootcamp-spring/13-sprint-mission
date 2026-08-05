package com.sprint.mission.discodeit.dto.channel;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ChannelUpdateRequest(
        UUID id,

        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description
) {
}
