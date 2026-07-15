package com.sprint.mission.discodeit.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

// ReadStatus 수정 요청 DTO
@Getter
@NoArgsConstructor
public class ReadStatusUpdateRequest {

    // 수정할 ReadStatus의 id
    private UUID id;

    /*
     * API 명세 v1.2 기준 필드명은 newLastReadAt
     * 기존 코드/프론트 호환을 위해 lastReadAt도 같이 허용
     */
    @JsonAlias({"newLastReadAt", "lastReadAt"})
    private Instant lastReadAt;

    public ReadStatusUpdateRequest(UUID id, Instant lastReadAt) {
        this.id = id;
        this.lastReadAt = lastReadAt;
    }

    /*
     * API 명세 v1.2 호환용 getter
     */
    public Instant getNewLastReadAt() {
        return lastReadAt;
    }
}