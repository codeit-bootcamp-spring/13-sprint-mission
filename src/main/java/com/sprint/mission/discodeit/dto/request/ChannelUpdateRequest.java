package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ChannelUpdateRequest(
    @Size(max = 100)
    String newName,
    String newDescription) {

}
