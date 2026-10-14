package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final ObjectMapper objectMapper;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;

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
  }
}
