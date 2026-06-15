package com.sprint.mission.discodeit.dto.input;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
public class UpdateUserStatusInput {
    private UUID ID;
    private Instant lastLoginTime;
}
