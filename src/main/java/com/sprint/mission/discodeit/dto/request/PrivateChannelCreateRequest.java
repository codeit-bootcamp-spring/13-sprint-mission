package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.entity.ChannelType;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record PrivateChannelCreateRequest(

        @NotNull(message = "ChannelType은 필수입니다.")
        ChannelType type,

        @NotNull(message = "userIdList는 필수입니다.")
        List<UUID> userIdList
) {
}
