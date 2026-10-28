package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.service.UserPresenceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

  private final RefreshTokenCookieProvider refreshTokenCookieProvider;
  private final UserPresenceService userPresenceService;

  @Override
  @CacheEvict(cacheNames = "users", allEntries = true)
  public void logout(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) {
    refreshTokenCookieProvider.clearCookie(response);

    if (authentication != null
        && authentication.getPrincipal() instanceof DiscodeitUserDetails userDetails) {
      userPresenceService.update(userDetails.getUserDto().id(), false);
    }
  }
}