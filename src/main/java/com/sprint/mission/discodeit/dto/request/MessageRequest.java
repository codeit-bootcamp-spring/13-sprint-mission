package com.sprint.mission.discodeit.dto.request;

import java.util.*;

public class MessageRequest {

    private MessageRequest() {
        throw new IllegalStateException("Utility class");
    }

    public record Create(
            UUID authorId,
            UUID channelId,
            String content
    ) {
    }

    public record Update(
            String content
    ) {
    }
}
