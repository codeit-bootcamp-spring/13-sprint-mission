package com.sprint.mission.discodeit.dto.input;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
public class CreateMessageInput {
    private UUID userID;
    private UUID channelID;
    private String message;
    private Set<UUID> dataIDs;
}
