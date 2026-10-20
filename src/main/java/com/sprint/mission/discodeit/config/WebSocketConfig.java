package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.security.StompJwtChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor;
import org.springframework.security.messaging.access.intercept.MessageAuthorizationContext;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final StompJwtChannelInterceptor stompJwtChannelInterceptor;
  private final RoleHierarchy roleHierarchy;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/sub");
    registry.setApplicationDestinationPrefixes("/pub");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
        .setAllowedOrigins(
            "http://localhost:8080",
            "http://localhost:8081",
            "http://localhost:8082"
        )
        .withSockJS();
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {

    AuthorityAuthorizationManager<MessageAuthorizationContext<?>> userRole =
        AuthorityAuthorizationManager.hasRole("USER");

    userRole.setRoleHierarchy(roleHierarchy);

    AuthorizationManager<Message<?>> authorizationManager =
        MessageMatcherDelegatingAuthorizationManager.builder()
            .simpTypeMatchers(SimpMessageType.CONNECT)
            .authenticated()
            .simpTypeMatchers(
                SimpMessageType.MESSAGE,
                SimpMessageType.SUBSCRIBE
            )
            .access(userRole)
            .simpTypeMatchers(
                SimpMessageType.DISCONNECT,
                SimpMessageType.UNSUBSCRIBE,
                SimpMessageType.HEARTBEAT
            )
            .permitAll()
            .anyMessage()
            .denyAll()
            .build();

    registration.interceptors(
        stompJwtChannelInterceptor,
        new SecurityContextChannelInterceptor(),
        new AuthorizationChannelInterceptor(authorizationManager)
    );
  }
}
