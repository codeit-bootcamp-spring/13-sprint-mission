package com.sprint.mission.discodeit.dto.input;

import java.util.Set;
import java.util.UUID;


public record CreateMessageInput (
        UUID userID,
        UUID channelID,
        String message,
        Set<UUID> dataIDs
){}
