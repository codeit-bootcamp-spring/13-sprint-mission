package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;

@Getter
public class ReadStatus implements Serializable {
    @Serial
    private static final long serialVersionUID=1L;
    private final UUID id;
    private final Instant createdAt;
    private Instant updatedAt; // 공통 필드
    private UUID userId;
    private UUID channelId;
    private Instant lastReadAt; // ReadAt -> lastReadAt으로 변경하여 마지막으로 읽은 시간임을 알려준다

    // 채널 별 마지막으로 메시지 읽은 시간 표현
    // 사용자별 각 채널에 읽지 않은 메시지 확인하기 위해 활용

    public ReadStatus(UUID userId, UUID channelId, Instant lastReadAt) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.channelId = channelId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.lastReadAt = lastReadAt;
    }

    public void update(Instant newLastReadAt) {
        boolean anyValueUpdated = false;
        if (newLastReadAt != null && !newLastReadAt.equals(this.lastReadAt)){
            this.lastReadAt = newLastReadAt;
            anyValueUpdated=true;
        }
        if (!anyValueUpdated){
            throw new IllegalArgumentException("변경사항이 없습니다.");
        }
        this.updatedAt=Instant.now();
    }
}


