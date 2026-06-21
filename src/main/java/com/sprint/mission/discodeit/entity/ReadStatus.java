package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
//사용자가 채널별 마지막으로 메시지를 읽은 시간을 표현하는 도메인 모델(사용자별 각 채널에 읽지 않은 메시지를 확인하기 위해 활용됨)
public class ReadStatus implements Serializable {
    private static final long sSERIAL_VERSION_UID = 1L;
    private final UUID id;
    private UUID userId;
    private UUID channelId;

    private Instant lastReadAt;

    private Instant updatedAt;
    private final Instant createdAt;

    public ReadStatus(UUID userId, UUID channelId, Instant lastReadAt) {

        this.id = UUID.randomUUID();
        this.userId = userId;
        this.channelId = channelId;
        this.lastReadAt = lastReadAt;
        this.updatedAt = Instant.now();
        this.createdAt = Instant.now();
    }

    public void update(Instant newLastReadAt) {
        boolean anyValueUpdated = false;
        if (newLastReadAt != null && !newLastReadAt.equals(lastReadAt)) {
            this.lastReadAt = newLastReadAt;
            anyValueUpdated = true;
        }
        if (anyValueUpdated) {
            this.updatedAt = Instant.now();
        }
    }

}
