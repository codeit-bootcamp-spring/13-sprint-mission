package com.sprint.mission.discodeit.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReadStatusCreateRequest{
        private UUID userId;
        private UUID channelId;
        private Instant lastReadAt;
}
