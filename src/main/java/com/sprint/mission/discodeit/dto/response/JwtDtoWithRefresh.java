package com.sprint.mission.discodeit.dto.response;

public record JwtDtoWithRefresh(JwtDto jwtDto, String refreshToken) {
}
