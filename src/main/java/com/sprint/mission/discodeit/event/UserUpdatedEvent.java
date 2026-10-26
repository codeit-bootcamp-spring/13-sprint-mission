package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.response.UserDto;

public record UserUpdatedEvent(
        UserDto userDto
) {
}
