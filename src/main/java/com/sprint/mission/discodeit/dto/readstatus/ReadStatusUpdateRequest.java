package com.sprint.mission.discodeit.dto.readstatus;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Setter
@Getter
@ToString
public class ReadStatusUpdateRequest {
    private Instant newLastReadAt;
}
