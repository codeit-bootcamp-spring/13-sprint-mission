package com.sprint.mission.discodeit.security;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class StompJwtChannelInterceptor implements ChannelInterceptor {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;
  private final CustomUserDetailsService userDetailsService;
  private final ChannelSecurity channelSecurity;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor == null) {
      return message;
    }

    StompCommand command = accessor.getCommand();

    if (command == StompCommand.DISCONNECT) {
      return message;
    }

    if (command == StompCommand.SEND || command == StompCommand.SUBSCRIBE) {

      if (!(accessor.getUser() instanceof Authentication authentication)
          || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
        throw new AccessDeniedException("인증이 필요합니다.");
      }

      String destination = accessor.getDestination();

      if (command == StompCommand.SEND) {
        if (!"/pub/messages".equals(destination)) {
          throw new AccessDeniedException("허용되지 않은 메시지 전송 경로입니다.");
        }
      }

      if (command == StompCommand.SUBSCRIBE) {
        String prefix = "/sub/channels.";
        String suffix = ".messages";

        if (destination == null
            || !destination.startsWith(prefix)
            || !destination.endsWith(suffix)) {
          throw new AccessDeniedException("허용되지 않은 구독 경로입니다.");
        }

        String channelIdText = destination.substring(
            prefix.length(),
            destination.length() - suffix.length()
        );

        UUID channelId;

        try {
          channelId = UUID.fromString(channelIdText);
        } catch (IllegalArgumentException e) {
          throw new AccessDeniedException("잘못된 채널 ID입니다.", e);
        }

        if (!channelSecurity.canAccess(
            channelId,
            userDetails.getUserDto().id()
        )) {
          throw new AccessDeniedException("구독 권한이 없는 채널입니다.");
        }
      }

      return message;
    }

    if (command != StompCommand.CONNECT) {
      return message;
    }

    String authorization = accessor.getFirstNativeHeader("Authorization");

    if (!StringUtils.hasText(authorization)
        || !authorization.startsWith("Bearer ")) {
      throw new AccessDeniedException("인증 토큰이 없습니다.");
    }

    String accessToken = authorization.substring(7);

    if (!jwtTokenProvider.validateAccessToken(accessToken)
        || !jwtRegistry.hasActiveJwtInformationByAccessToken(accessToken)) {
      throw new AccessDeniedException("유효하지 않은 인증 토큰입니다.");
    }

    CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(
        jwtTokenProvider.getEmail(accessToken)
    );

    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
        userDetails,
        null,
        userDetails.getAuthorities()
    );

    accessor.setUser(authentication);

    return message;
  }
}
