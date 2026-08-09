package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.dto.command.*;
import jakarta.validation.constraints.*;

public record CreatePublicChannelRequest(

        @NotBlank(message = "채널 이름은 필수입니다.")
        @Size(max = 30, message = "채널 이름은 30자 이하여야 합니다.")
        String name,

        @Size(max = 200, message = "채널 소개는 200자 이하여야 합니다.")
        String description
) {

    public CreatePublicChannelCommand toCommand() {
        return new CreatePublicChannelCommand(name, description);
    }

}
