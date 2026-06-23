package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.ReadStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReadStatusResponse {

    private UUID id;
    private UUID userId;
    private UUID channelId;
    private Instant lastReadAt;

    public static ReadStatusResponse from(ReadStatus status) {
        return new ReadStatusResponse(
                status.getId(),
                status.getUserId(),
                status.getChannelId(),
                status.getLastReadAt()
        );
    }
}
