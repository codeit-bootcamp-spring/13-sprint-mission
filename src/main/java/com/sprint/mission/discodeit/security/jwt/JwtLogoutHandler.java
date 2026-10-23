package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.security.jwt.registry.JwtRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;

    private final ApplicationEventPublisher eventPublisher;

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
                            if(jwtRegistry.hasActiveJwtInformationByRefreshToken(token))
                                // 쿠키를 사용한 로그아웃 감지 로직
                                jwtTokenProvider.getSubject(token);

                                // jwtRegistry 등록 쿠키 삭제.
                                jwtRegistry.invalidateJwtInformationByUserId(
                                        UUID.fromString(jwtTokenProvider.getSubject(token))
                                );
                        }
                );

        log.debug("[LogoutHandler] - 유저 로그아웃. 이벤트 발행");
//        eventPublisher.publishEvent(new UserEvent("users.updated", principal.getUserDto()));
    }

}
