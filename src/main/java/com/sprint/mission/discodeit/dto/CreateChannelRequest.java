package com.sprint.mission.discodeit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateChannelRequest {

    @NotBlank(message = "채널 이름은 필수입니다.")
    @Size(
            max = 100,
            message = "채널 이름은 100자 이하여야 합니다."
    )
    private String name;

    @Size(
            max = 500,
            message = "채널 설명은 500자 이하여야 합니다."
    )
    private String description;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}