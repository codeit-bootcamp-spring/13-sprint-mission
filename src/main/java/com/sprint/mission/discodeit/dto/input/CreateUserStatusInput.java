package com.sprint.mission.discodeit.dto.input;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@Builder
public class CreateUserStatusInput {
    private UUID userID;
    private Instant loginTime;
}
