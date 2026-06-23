package com.sprint.mission.discodeit.dto.input;


import java.time.Instant;
import java.util.UUID;

public record CreateUserStatusInput (
     UUID userID,
     Instant loginTime
) {}
