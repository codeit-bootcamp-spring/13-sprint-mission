package com.sprint.mission.discodeit.dto.readstatus;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@ToString
public class ReadStatusCreateRequest {
    private UUID userId;
    private UUID channelId;
    private Instant lastReadAt;
}
