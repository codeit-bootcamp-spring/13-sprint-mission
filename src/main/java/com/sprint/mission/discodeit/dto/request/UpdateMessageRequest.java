package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.dto.command.*;
import jakarta.validation.constraints.*;

public record UpdateMessageRequest(

        @NotBlank(message = "메세지 내용은 필수입니다.")
        @Size(max = 2000, message = "메세지의 내용은  2,000자 이하여야 합니다.")
        String content
) {
    public UpdateMessageCommand toCommand() {
        return new UpdateMessageCommand(content);
    }
}
