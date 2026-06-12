package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.entity.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PublicChannelCreateRequest(

        @NotNull(message = "ChannelType은 필수입니다.")
        ChannelType type,

        @NotBlank(message = "name은 비워둘 수 없습니다.")
        String name,

        @NotBlank(message = "description은 비워둘 수 없습니다.")
        String description
) {
}
