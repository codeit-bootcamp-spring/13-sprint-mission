package com.sprint.mission.discodeit.dto.input;

import java.util.UUID;

public record PublicChannelUpdateRequest(
        String newName,
        String newDescription
){}
