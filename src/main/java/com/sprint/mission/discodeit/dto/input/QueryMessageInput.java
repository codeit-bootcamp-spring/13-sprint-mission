package com.sprint.mission.discodeit.dto.input;

import java.util.UUID;

public record QueryMessageInput (
        UUID channel,
        UUID user
){
}
