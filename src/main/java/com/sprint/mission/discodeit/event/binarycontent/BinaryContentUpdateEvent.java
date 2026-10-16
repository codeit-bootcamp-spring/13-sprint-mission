package com.sprint.mission.discodeit.event.binarycontent;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import lombok.Getter;

import java.util.UUID;

@Getter
public class BinaryContentUpdateEvent {
    private final UUID id = UUID.randomUUID();
    private final String name = "binaryContents.update";
    private final BinaryContentDto dto;

    public BinaryContentUpdateEvent(BinaryContentDto dto) {
        this.dto = dto;
    }
}
