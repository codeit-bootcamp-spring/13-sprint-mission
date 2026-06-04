package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.time.Instant;
import java.util.UUID;

public record ReadStatusUpdateResponse(
        UUID readStatusId,
        UUID userId,
        UUID channelId,
        Instant lastAccessTime
) {
    public static ReadStatusUpdateResponse from(ReadStatus readStatus) {
        return new ReadStatusUpdateResponse(
                readStatus.getId(),
                readStatus.getUserId(),
                readStatus.getChannelId(),
                readStatus.getLastAccessTime()
        );
    }
}
