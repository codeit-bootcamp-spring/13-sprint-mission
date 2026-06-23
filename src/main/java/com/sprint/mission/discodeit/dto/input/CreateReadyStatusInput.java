package com.sprint.mission.discodeit.dto.input;

import java.util.UUID;

public record CreateReadyStatusInput (
        UUID userID,
        UUID channelID
){}
