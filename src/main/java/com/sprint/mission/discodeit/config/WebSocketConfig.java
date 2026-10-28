package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.security.ChannelSubscriptionAuthorizationManager;
import com.sprint.mission.discodeit.security.JwtStompAuthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor;
import org.springframework.security.messaging.access.intercept.MessageAuthorizationContext;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP 목적지, SockJS 접속 경로, 수신 프레임의 인증·인가 순서를 구성한다.
 * SimpleBroker는 서버별 메모리에 존재하며 서버 간 메시지 전달은 별도 발행자가 담당한다.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtStompAuthenticationInterceptor jwtStompAuthenticationInterceptor;
    private final ChannelSubscriptionAuthorizationManager channelSubscriptionAuthorizationManager;
    private final RoleHierarchy roleHierarchy;

    /**
     * /pub 메시지는 애플리케이션 컨트롤러로, /sub 구독은 메모리 브로커로 라우팅한다.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub");
    }

    /**
     * /ws를 STOMP 연결 진입점으로 등록하고 SockJS 대체 전송도 지원한다.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").withSockJS();
    }

    /**
     * JWT 인증 → SecurityContext 연결 → 프레임 인가 순서로 처리한다.
     * 채팅 발행과 채널 구독 경로를 명시적으로 허용하고 나머지 메시지는 거부한다.
     * 종료·구독 해제·heartbeat 프레임은 연결 정리가 막히지 않도록 허용한다.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {

        AuthorityAuthorizationManager<MessageAuthorizationContext<?>> userRoleManger = userRoleAuthorizationManager();
        MessageMatcherDelegatingAuthorizationManager.Builder authorization = MessageMatcherDelegatingAuthorizationManager.builder();


        authorization.simpTypeMatchers(SimpMessageType.CONNECT)
                .access(userRoleManger)
                .simpTypeMatchers(
                        SimpMessageType.DISCONNECT,
                        SimpMessageType.UNSUBSCRIBE,
                        SimpMessageType.HEARTBEAT
                )
                .permitAll()
                .simpMessageDestMatchers("/pub/messages")
                .access(userRoleManger)
                .simpSubscribeDestMatchers("/sub/channels.{channelId}.messages")
                .access(AuthorizationManagers.allOf(
                        userRoleManger,
                        channelSubscriptionAuthorizationManager
                        )
                )
                .anyMessage().denyAll();


        registration.interceptors(
                jwtStompAuthenticationInterceptor,
                new SecurityContextChannelInterceptor(),
                new AuthorizationChannelInterceptor(authorization.build())
        );
    }

    private AuthorityAuthorizationManager<MessageAuthorizationContext<?>> userRoleAuthorizationManager(){
        AuthorityAuthorizationManager<MessageAuthorizationContext<?>> manager = AuthorityAuthorizationManager.hasRole(Role.USER.name());

        manager.setRoleHierarchy(roleHierarchy);

        return manager;
    }
}
