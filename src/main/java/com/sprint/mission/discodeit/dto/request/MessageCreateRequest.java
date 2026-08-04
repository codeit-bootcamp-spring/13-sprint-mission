package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;


public record MessageCreateRequest(
        @NotBlank String content,
        @NotBlank UUID channelId,
        @NotBlank UUID authorId
){}
