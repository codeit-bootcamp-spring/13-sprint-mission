package com.sprint.mission.discodeit.dto.input;


import java.time.Instant;

public record ReadStatusUpdateRequest (
        Instant newLastReadAt
) {}
