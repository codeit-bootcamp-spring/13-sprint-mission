package com.sprint.mission.discodeit.dto.input;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class UserProfile {
    private final String name;
    private final UUID thumbnail;
}
