package com.sprint.mission.discodeit.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

// User 온라인 상태 수정 요청 DTO
@Getter
@NoArgsConstructor
public class UserStatusUpdateRequest {

    /*
     * API 명세 v1.2 기준 필드명은 newLastActiveAt
     * 기존 코드/프론트 호환을 위해 lastActiveAt, lastSeenAt도 같이 허용
     */
    @NotNull(message = "마지막 활동 시각은 필수입니다.")
    @PastOrPresent(message = "마지막 활동 시각은 현재 또는 과거여야 합니다.")
    @JsonAlias({"newLastActiveAt", "lastActiveAt", "lastSeenAt"})
    private Instant lastActiveAt;

    public UserStatusUpdateRequest(Instant lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    /*
     * API 명세 v1.2 호환용 getter
     */
    public Instant getNewLastActiveAt() {
        return lastActiveAt;
    }
}