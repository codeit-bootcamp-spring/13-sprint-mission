package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.*;

import java.time.*;
import java.util.*;

public record ReadStatusResponse(
        UUID id,
        UUID userId,
        UUID channelId,
        Instant lastReadTime
) {
    public static ReadStatusResponse from(ReadStatus readStatus) {
        return new ReadStatusResponse(
                readStatus.getId(),
                readStatus.getUserId(),
                readStatus.getChannelId(),
                readStatus.getLastReadTime()
        );
    }
}