package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.SseDomainEventPublisher;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;
  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final SseDomainEventPublisher sseDomainEventPublisher;

  @CacheEvict(cacheNames = "users", allEntries = true)
  @Override
  public void logout(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication) {

    ResponseCookie deletedCookie = jwtTokenProvider.createRefreshTokenDeleteCookie();

    response.addHeader(
        HttpHeaders.SET_COOKIE, deletedCookie.toString()
    );

    Cookie[] cookies = request.getCookies();

    if (cookies == null) {
      return;
    }

    Arrays.stream(cookies)
        .filter(cookie -> JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName()))
        .map(Cookie::getValue)
        .filter(jwtRegistry::hasActiveJwtInformationByRefreshToken)
        .findFirst()
        .ifPresent(refreshToken -> {
          UUID userId = jwtTokenProvider.getUserId(refreshToken);

          jwtRegistry.invalidateJwtInformationByUserId(userId);

          try {
            userRepository.findById(userId).ifPresent(user -> {
              UserDto userDto = userMapper.toDto(user)
                  .withOnline(false);

              sseDomainEventPublisher.publishToAllUsers(
                  "users.updated",
                  userDto
              );

              log.info(
                  "로그아웃 오프라인 상태 SSE 이벤트 발행: userId={}",
                  userId
              );
            });

          } catch (RuntimeException e) {
            log.warn(
                "로그아웃 오프라인 상태 SSE 이벤트 발행 실패: userId={}",
                userId,
                e
            );
          }
        });
  }
}
