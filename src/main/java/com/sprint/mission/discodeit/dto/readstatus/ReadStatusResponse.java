package com.sprint.mission.discodeit.dto.readstatus;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.time.Instant;
import java.util.UUID;

public record ReadStatusResponse(
        UUID channelId,
        UUID userId,
        Instant readAt
) {
    public static ReadStatusResponse from(ReadStatus readStatus) {
        return new ReadStatusResponse(
                readStatus.getChannelId(),
                readStatus.getUserId(),
                readStatus.getReadAt()
        );
    }
}
