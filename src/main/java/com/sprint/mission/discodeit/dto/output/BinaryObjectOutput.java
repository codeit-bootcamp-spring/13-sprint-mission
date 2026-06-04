package com.sprint.mission.discodeit.dto.output;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
@Builder
public class BinaryObjectOutput {
    UUID contentID;
}
