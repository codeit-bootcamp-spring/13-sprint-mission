package com.sprint.mission.discodeit.security.jwt;


import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

    private final RefreshTokenService refreshTokenService;
    private final JwtRegistry jwtRegistry;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        /*
        현재 permitAll() 이라 Authentication 없을 수 도.
        -> refreshToken 을 수명이 0 인 토큰으로 발급
        && 기존 토큰을 제거 후 응답해서 기존 리프레시 토큰을 삭제.
         */

        Arrays.stream(request.getCookies())
                .filter(
                        cookie -> cookie.getName()
                                .equals("REFRESH_TOKEN")    // 리프레시 토큰 이름
                )
                .findFirst()
                .ifPresent(
                        cookie -> {
                            // 기존 리프레시 토큰 비활성화
                            // 엑세스 토큰 로테이트 시에, 리프레시 작업 에러로 로그인 방지.
                            String token = cookie.getValue();
                            User user = refreshTokenService.getUserFrom(token)
                                    .orElseThrow(UserNotFoundException::new);

                            refreshTokenService.revoke(token);
                            // jwtRegistry 등록 쿠키 삭제.
                            jwtRegistry.invalidateJwtInformationByUserId(user.getId());
                        }
                );
    }

}
