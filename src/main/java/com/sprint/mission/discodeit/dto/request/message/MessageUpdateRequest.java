package com.sprint.mission.discodeit.dto.request.message;


import jakarta.validation.constraints.NotBlank;

public record MessageUpdateRequest(
        @NotBlank String newContent
) {}
