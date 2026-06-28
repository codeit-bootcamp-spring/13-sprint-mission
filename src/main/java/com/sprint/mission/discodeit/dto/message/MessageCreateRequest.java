package com.sprint.mission.discodeit.dto.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;

import java.util.List;
import java.util.UUID;

public record MessageCreateRequest(
        String content,
        UUID channelId,

        @JsonProperty("authorId")
        UUID userId,

        List<BinaryContentCreateRequest> attachments
) {
}
