package com.sprint.mission.discodeit.dto.response;

public record JwtRefreshResult(
        JwtDto jwtDto,
        String refreshToken
) {
}
