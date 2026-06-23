package com.sprint.mission.discodeit.dto.input;

import java.util.UUID;

public record BinaryContentInput (
        UUID auth,
        UUID content
) {}
