package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record PrivateChannelCreateRequest(

        @NotEmpty(message = "최소 1명은 존재해야 합니다.")
        List<UUID> userIds
) {
}
