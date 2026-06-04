package com.sprint.mission.discodeit.dto.input;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
public class UpdateMessageInput {
    private UUID messageID;
    private String text;
    private Set<UUID> dataIDs;
}
