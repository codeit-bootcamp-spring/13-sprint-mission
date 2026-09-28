package com.sprint.mission.discodeit.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtSuccessHandler implements AuthenticationSuccessHandler {

    private String SERVICE_NAME = "JWT_LOGIN";

    private final ObjectMapper objectMapper;

    private final JwtTokenProvider jwtTokenProvider;

    private final RefreshTokenService refreshTokenService;

    private final JwtRegistry jwtRegistry;

    /**
     * 로그인에 성공하면 UserDetail 에 존재하는 유저 정보 반환.
     * @param request the request which caused the successful authentication
     * @param response the response
     * @param authentication the <tt>Authentication</tt> object which was created during
     * the authentication process.
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        /*
        Authentication.principal(DaoAuthenticationProvider 가 세팅한 객체) == DiscodeitUserDetails
        를 이용, 토큰을 세팅하고 클라이언트에게 반환.
         */

        log.debug(
                "{} - 로그인 요청 들어옴.\nprincipal(id): {}\ncredential(pw):{}",
                SERVICE_NAME,
                authentication.getPrincipal(),
                authentication.getCredentials() // maybe null
        );

        DiscodeitUserDetails principal = (DiscodeitUserDetails) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.createAccessToken(
                principal.getUsername(),    // user.id
                principal.getUserDto().role()
        );

        String refreshToken = refreshTokenService.grant(
                UUID.fromString(principal.getUsername())
        );


        // refresh token 쿠키 세팅
        Cookie cookie = new Cookie("REFRESH_TOKEN", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(jwtTokenProvider.refreshTokenExpire());

        response.addCookie(cookie);

        // access Token 응답 매핑
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // jwt 레지스트리에 등록 (동시 로그인 세션을 제어.)
        jwtRegistry.registerJwtInformation(
                new JwtInformation(
                        principal.getUserDto(),
                        accessToken,
                        refreshToken
                )
        );

        // todo - 읍답 반환시, 에러 생기면 Error Response 로 반환.

        // 클라이언트에 응답 반환.
        objectMapper.writeValue(
                response.getWriter(),
                new JwtDto(
                        principal.getUserDto(),
                        accessToken
                )
        );
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authentication
    ) throws IOException, ServletException {
        AuthenticationSuccessHandler
                .super
                .onAuthenticationSuccess(request, response, chain, authentication);
    }
}
