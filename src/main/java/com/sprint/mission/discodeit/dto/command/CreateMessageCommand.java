package com.sprint.mission.discodeit.dto.command;

import com.sprint.mission.discodeit.dto.request.*;

import java.util.*;

public record CreateMessageCommand(
        UUID authorId,
        UUID channelId,
        String content
) {

    public CreateMessageCommand from(CreateMessageRequest request) {
        return new CreateMessageCommand(
                request.authorId(),
                request.channelId(),
                request.content()
        );
    }
}

