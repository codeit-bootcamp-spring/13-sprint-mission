package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.response.JwtDtoWithRefresh;
import com.sprint.mission.discodeit.dto.response.TokenDto;
import com.sprint.mission.discodeit.dto.response.UserDto;

public interface TokenService {
    /**
     * 현재 HTTP 요청의 REFRESH_TOKEN 쿠키로 사용자 정보와 새 토큰을 발급한다.
     * 현재는 재발급만 수행하며, 사용한 토큰의 폐기·재사용 방지는 후속 메모리 저장소 작업에서 구현한다.
     */
    JwtDtoWithRefresh rotateRefreshToken();

    TokenDto generateToken(UserDto userDto);

    void addRefreshTokenCookie(String refreshToken);
}
