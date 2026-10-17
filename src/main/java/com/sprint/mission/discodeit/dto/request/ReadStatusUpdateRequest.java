package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record ReadStatusUpdateRequest(
        @NotNull(message = "마지막으로 읽은 시각은 필수입니다.")
        Instant lastReadAt,

        @NotNull(message = "알림 설정 여부는 필수입니다.")
        Boolean notificationEnabled
) {
}