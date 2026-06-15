package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
//사용자가 채널별 마지막으로 메시지를 읽은 시간을 표현하는 도메인 모델(사용자별 각 채널에 읽지 않은 메시지를 확인하기 위해 활용됨)
public class ReadStatus implements Serializable {
    private static final long serialVersionUID = 1L;
    private UUID id;
    private UUID userId;
    private UUID channelId;

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

    public void markAsRead() { //마지막 읽은 시간 갱신
        this.lastReadAt = Instant.now();
        this.updatedAt = Instant.now();
    }

}
