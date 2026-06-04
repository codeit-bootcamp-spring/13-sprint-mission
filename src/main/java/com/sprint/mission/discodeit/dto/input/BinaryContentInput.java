package com.sprint.mission.discodeit.dto.input;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@Builder
public class BinaryContentInput {
    private final UUID authorID;
    private final UUID contentID;
}
