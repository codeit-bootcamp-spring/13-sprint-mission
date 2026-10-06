package com.sprint.mission.discodeit.dto;

public record TokenRefreshResult(
        JwtDto jwtDto,
        String refreshToken
) {
}