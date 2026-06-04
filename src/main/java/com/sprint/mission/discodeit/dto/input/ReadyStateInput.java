package com.sprint.mission.discodeit.dto.input;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Builder
public class ReadyStateInput {
    UUID userID;
    UUID channelID;
}
