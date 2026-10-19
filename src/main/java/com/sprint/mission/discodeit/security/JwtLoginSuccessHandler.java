package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.service.SseDomainEventPublisher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final ObjectMapper objectMapper;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;
  private final SseDomainEventPublisher sseDomainEventPublisher;

  @CacheEvict(cacheNames = "users", allEntries = true)
  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) throws IOException {

    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

    UserDto userDto = userDetails.getUserDto().withOnline(true);

    String accessToken = jwtTokenProvider.createAccessToken(userDto);

    String refreshToken = jwtTokenProvider.createRefreshToken(userDto);

    jwtRegistry.registerJwtInformation(
        new JwtInformation(userDto, accessToken, refreshToken)
    );

    ResponseCookie refreshCookie = jwtTokenProvider.createRefreshTokenCookie(refreshToken);

    response.addHeader(
        HttpHeaders.SET_COOKIE, refreshCookie.toString()
    );

    JwtDto jwtDto = new JwtDto(
        userDto, accessToken
    );

    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    objectMapper.writeValue(
        response.getWriter(),
        jwtDto
    );

    try {
      sseDomainEventPublisher.publishToAllUsers(
          "users.updated",
          userDto
      );

      log.info(
          "로그인 온라인 상태 SSE 이벤트 발행: userId={}",
          userDto.id()
      );

    } catch (RuntimeException e) {
      log.warn(
          "로그인 온라인 상태 SSE 이벤트 발행 실패: userId={}",
          userDto.id(),
          e
      );
    }
  }
}
