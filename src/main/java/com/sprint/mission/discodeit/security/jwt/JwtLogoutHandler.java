package com.sprint.mission.discodeit.security.jwt;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

    private final RefreshTokenService refreshTokenService;

    private final String BEARER_PREFIX = "bearer";

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
    {
        String token = resolveToken(request);

        // 정상적인 로그아웃 시, 현재 기기의 리프레시 토큰 삭제.
        // 다른 기기의 토큰은 잔류.
        refreshTokenService.revoke(token);
    }

    /*
    유틸리티 매서드
     */
    private String resolveToken(HttpServletRequest request){
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(header != null && header.startsWith(BEARER_PREFIX))
            return header.substring(BEARER_PREFIX.length());
        return null;
    }

}
