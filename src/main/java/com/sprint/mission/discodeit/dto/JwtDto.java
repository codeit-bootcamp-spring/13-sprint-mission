package com.sprint.mission.discodeit.dto;

public record JwtDto(
    UserDto userDto,
    String accessToken
) {

}