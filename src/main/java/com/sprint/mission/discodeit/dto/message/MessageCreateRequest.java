package com.sprint.mission.discodeit.dto.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record MessageCreateRequest(
        String content,

        @NotNull
        UUID channelId,

        @JsonProperty("authorId")
        @NotNull
        UUID userId,

        @Valid
        List<BinaryContentCreateRequest> attachments
) {
}
