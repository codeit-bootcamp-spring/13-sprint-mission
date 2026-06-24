package com.sprint.mission.discodeit.dto.userstatus;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Setter
@Getter
@ToString
public class UserStatusUpdateRequest {
    private Instant newLastActiveAt;
}
