package com.sprint.mission.discodeit.dto.input;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class UpdateReadStatusInput {
    private UUID readStatusID;
    private UUID userID;
    private UUID channelID;
}
