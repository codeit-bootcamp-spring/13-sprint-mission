package com.sprint.mission.discodeit.dto.input;

import java.util.UUID;

public record UpdateChannelInput (String id, String name, String description){
    public UUID idToUUID (){ return UUID.fromString(id); }
}
