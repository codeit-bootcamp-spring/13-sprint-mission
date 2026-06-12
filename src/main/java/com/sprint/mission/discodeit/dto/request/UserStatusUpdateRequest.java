package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UserStatusUpdateRequest(

        @NotNull(message = "userStatusId는 필수입니다.")
        UUID userStatusId,

        @NotNull(message = "userId는 필수입니다.")
        UUID userId
) {
}
