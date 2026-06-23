package com.sprint.mission.discodeit.dto.input;


import java.util.List;
import java.util.UUID;

public record PrivateChannelCreateRequest(
        List<UUID> participantIds
){}
