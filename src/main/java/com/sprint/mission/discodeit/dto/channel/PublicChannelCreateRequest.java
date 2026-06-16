package com.sprint.mission.discodeit.dto.channel;

import com.sprint.mission.discodeit.entity.ChannelType;

public record PublicChannelCreateRequest(
        String name,
        String description // 기존 로직 유지
) { }
