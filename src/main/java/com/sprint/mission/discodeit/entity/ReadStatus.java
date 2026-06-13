package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public class ReadStatus implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final Instant createdAt;
    private Instant updatedAt;
    private final UUID userId;
    private final UUID channelId;

    public ReadStatus(UUID userId, UUID channelId) {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.userId = userId;
        this.channelId = channelId;
    }

    // 마지막으로 읽은 시간을 갱신
    public void updateLastRead() {
        this.updatedAt = Instant.now();
    }

}



/*
[ ]  ReadStatus
"사용자가 채널 별 마지막으로 메시지를 읽은 시간을 표현"하는 도메인 모델입니다. 사용자별 각 채널에 읽지 않은 메시지를 확인하기 위해 활용합니다.
-> 이 시간 이후에, 안 읽은 메세지를 알림으로 알려주기 위해서(이 시간을 기준으로 기준 후의 데이터를 따로 식별하기 위해!)
 */