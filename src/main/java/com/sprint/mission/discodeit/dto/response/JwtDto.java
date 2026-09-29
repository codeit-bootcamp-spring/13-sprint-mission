package com.sprint.mission.discodeit.dto.response;

public record JwtDto(
        UserDto userDto,
        String accessToken
) {
}
