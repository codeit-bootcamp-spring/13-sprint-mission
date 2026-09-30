package com.sprint.mission.discodeit.dto.response;

public record TokenDto(
        String accessToken,
        String refreshToken
) {
}
