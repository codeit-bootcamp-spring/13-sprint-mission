package com.sprint.mission.discodeit.dto.command;

import com.sprint.mission.discodeit.dto.request.*;
import jakarta.validation.constraints.*;

public record UpdateMessageCommand(
        String content
) {
        public UpdateMessageCommand from(UpdateMessageRequest request) {
                return new UpdateMessageCommand(request.content());
        }
}
