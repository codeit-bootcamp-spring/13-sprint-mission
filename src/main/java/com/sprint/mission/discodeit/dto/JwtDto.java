package com.sprint.mission.discodeit.dto;

public record JwtDto(
        UserResponse userDto,
        String accessToken
) {
}