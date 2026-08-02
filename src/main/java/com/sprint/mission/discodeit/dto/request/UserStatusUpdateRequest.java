package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record UserStatusUpdateRequest(

        @NotNull(message = "사용자 상태 ID는 필수입니다.")
        UUID id,

        @NotNull(message = "업데이트 시간은 필수입니다.")
        Instant updatedAt
) {
}
