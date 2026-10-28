package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.security.JwtAuthenticationChannelInterceptor;
import com.sprint.mission.discodeit.realtime.ChannelAccess;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.config.*;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.messaging.access.intercept.*;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
  private final JwtAuthenticationChannelInterceptor authentication;
  private final ChannelAccess channelAccess;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/sub");
    registry.setApplicationDestinationPrefixes("/pub");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws").withSockJS();
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    var manager = MessageMatcherDelegatingAuthorizationManager.builder()
        .simpTypeMatchers(SimpMessageType.DISCONNECT, SimpMessageType.UNSUBSCRIBE,
            SimpMessageType.HEARTBEAT).permitAll()
        .simpSubscribeDestMatchers("/sub/channels.*.messages").access((auth, context) -> {
          String destination = SimpMessageHeaderAccessor.getDestination(context.getMessage().getHeaders());
          try {
            UUID channelId = UUID.fromString(destination.substring(
                "/sub/channels.".length(), destination.length() - ".messages".length()));
            return new AuthorizationDecision(channelAccess.canRead(channelId, auth.get()));
          } catch (IllegalArgumentException exception) {
            return new AuthorizationDecision(false);
          }
        })
        .simpTypeMatchers(SimpMessageType.SUBSCRIBE).denyAll()
        .simpDestMatchers("/pub/messages").hasAnyRole("USER", "CHANNEL_MANAGER", "ADMIN")
        .simpTypeMatchers(SimpMessageType.CONNECT).hasAnyRole("USER", "CHANNEL_MANAGER", "ADMIN")
        .anyMessage().denyAll().build();
    registration.interceptors(authentication, new SecurityContextChannelInterceptor(),
        new AuthorizationChannelInterceptor(manager));
  }
}
