package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record ReadStatusUpdateRequest(

        @NotNull(message = "읽기 상태 ID는 필수입니다.")
        UUID id,

        @NotNull(message = "마지막으로 읽은 시간은 필수입니다.")
        Instant lastReadAt
) {
}
