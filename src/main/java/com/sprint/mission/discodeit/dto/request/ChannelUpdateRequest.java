package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.entity.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ChannelUpdateRequest(

        @NotNull(message = "channelId는 필수입니다.")
        UUID channelId,

        @NotNull(message = "ChannelType은 필수입니다.")
        ChannelType type,

        @NotBlank(message = "name은 비워둘 수 없습니다.")
        String name,

        @NotBlank(message = "description은 비워둘 수 없습니다.")
        String description
) {
}
