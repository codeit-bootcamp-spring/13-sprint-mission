package com.sprint.mission.discodeit.dto.readstatus;

import com.sprint.mission.discodeit.entity.ReadStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ReadStatusResponse {
    private UUID channelId;
    private UUID userId;
    private Instant readAt;

    public static ReadStatusResponse from(ReadStatus readStatus) {
        return ReadStatusResponse.builder()
                .channelId(readStatus.getChannelId())
                .userId(readStatus.getUserId())
                .readAt(readStatus.getLastReadAt())
                .build();
    }
}
