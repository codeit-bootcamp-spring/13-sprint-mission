package com.sprint.mission.discodeit.dto.input;

import lombok.Builder;
import lombok.Setter;

import java.util.UUID;

@Setter
@Builder
public class IDRequestInput {
    private String id;

    public UUID getID(){
        return UUID.fromString(id);
    }

}
