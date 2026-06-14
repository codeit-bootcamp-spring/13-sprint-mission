package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

//사용자가 채널별 마지막으로 메시지를 읽은 시간을 표현하는 도메인 모델(사용자별 각 채널에 읽지 않은 메시지를 확인하기 위해 활용됨)
public class ReadStatus implements Serializable {
    private static final long serialVersionUID = 1L;
    private final UUID id;
    private final UUID userId;
    private final UUID channelId;

    private Instant lastReadAt;

    private Instant updatedAt;
    private final Instant createdAt;

    public ReadStatus(UUID getId,UUID channelId) {

        this.id = getId;
        this.userId = channelId;
        this.channelId = UUID.randomUUID();
        this.lastReadAt = Instant.now();
        this.updatedAt = Instant.now();
        this.createdAt = Instant.now();
    }

    public UUID getId() {return id;}
    public UUID getUserId() {return userId;}
    public UUID getChangeId() {return channelId;}
    public Instant getLastReadAt() {return lastReadAt;}
    public Instant getUpdatedAt() {return updatedAt;}
    public Instant getCreatedAt() {return createdAt;}

    public void markAsRead() { //마지막 읽은 시간 갱신
        this.lastReadAt = Instant.now();
        this.updatedAt = Instant.now();
    }

}
