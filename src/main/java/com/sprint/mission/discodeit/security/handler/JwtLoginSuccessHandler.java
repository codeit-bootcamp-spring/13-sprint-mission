package com.sprint.mission.discodeit.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.dto.response.TokenDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.events.UserCacheClearEvent;
import com.sprint.mission.discodeit.events.UserEvent;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.JwtInformation;
import com.sprint.mission.discodeit.security.JwtRegistry;
import com.sprint.mission.discodeit.service.TokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final TokenService tokenService;
    private final JwtRegistry jwtRegistry;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        DiscodeitUserDetails discodeitUser = (DiscodeitUserDetails) authentication.getPrincipal();
        UserDto body = discodeitUser.getUserDto();
        TokenDto tokenDto = tokenService.generateToken(body);

        jwtRegistry.registerJwtInformation(new JwtInformation(
                    body,
                    tokenDto.accessToken(),
                    tokenDto.refreshToken()
                )
        );

        JwtDto jwtDto = new JwtDto(body, tokenDto.accessToken());
        tokenService.addRefreshTokenCookie(tokenDto.refreshToken());

        eventPublisher.publishEvent(new UserCacheClearEvent(body.id()));
        // 로그인 과정에서 이미 준비한 온라인 DTO를 재사용하여 중복 사용자 조회 없이 상태를 알린다.
        eventPublisher.publishEvent(UserEvent.updateEvent(body));

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), jwtDto);
    }
}
