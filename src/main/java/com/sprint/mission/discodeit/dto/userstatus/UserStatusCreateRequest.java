package com.sprint.mission.discodeit.dto.userstatus;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@ToString
public class UserStatusCreateRequest {
    private UUID userId;
    private Instant lastActiveAt;
}
