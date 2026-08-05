package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.enums.ChannelType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChannelResponse(
        UUID id,
        String name,
        String description,
        ChannelType type,
        List<UserResponse> participants,
        Instant lastMessageAt
) {}
