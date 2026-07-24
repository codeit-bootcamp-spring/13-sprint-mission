package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.dto.command.*;
import jakarta.validation.constraints.*;

import java.util.*;

public record CreateMessageRequest(

        @NotNull(message = "작설자 ID는 필수입니다.")
        UUID authorId,

        @NotNull(message = "채널 ID는 필수입니다.")
        UUID channelId,

        @NotBlank(message = "매새자 내용은 필수압니다.")
        @Size(max = 2000, message = "메세지의 내용은  2,000자 이하여야 합니다.")
        String content
) {
    public CreateMessageCommand toCommand() {
        return new CreateMessageCommand(authorId, channelId, content);
    }
}

