package com.sprint.mission.discodeit.event.user;

import com.sprint.mission.discodeit.dto.response.UserDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class UserEvent {
    private final UUID id = UUID.randomUUID();
    private final String name;
    private final UserDto data;
}
