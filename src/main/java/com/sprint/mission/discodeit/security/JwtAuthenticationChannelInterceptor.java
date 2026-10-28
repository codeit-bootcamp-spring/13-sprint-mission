package com.sprint.mission.discodeit.security;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationChannelInterceptor implements ChannelInterceptor {
  private final JwtTokenProvider tokenProvider;
  private final JwtRegistry registry;
  private final UserDetailsService users;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
      String header = accessor.getFirstNativeHeader("Authorization");
      if (header == null || !header.startsWith("Bearer ")) {
        throw new BadCredentialsException("액세스 토큰이 필요합니다.");
      }
      String token = header.substring(7).trim();
      if (!tokenProvider.validateAccessToken(token)
          || !registry.hasActiveJwtInformationByAccessToken(token)) {
        throw new BadCredentialsException("유효하지 않은 액세스 토큰입니다.");
      }
      var user = users.loadUserByUsername(tokenProvider.getClaims(token).getSubject());
      accessor.setUser(UsernamePasswordAuthenticationToken.authenticated(
          user, null, user.getAuthorities()));
    }
    return message;
  }
}
