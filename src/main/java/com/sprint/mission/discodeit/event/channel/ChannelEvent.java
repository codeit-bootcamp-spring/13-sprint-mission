package com.sprint.mission.discodeit.event.channel;

import com.sprint.mission.discodeit.dto.response.ChannelDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class ChannelEvent {
    private final UUID id = UUID.randomUUID();
    private final String name;
    private final ChannelDto data;
}
