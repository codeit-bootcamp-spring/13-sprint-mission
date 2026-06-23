package com.sprint.mission.discodeit.dto.input;


import java.util.UUID;

public record UpdateUserInput (
        UUID id,
        String name,
        String pw,
        UUID thumbnail
) {}
