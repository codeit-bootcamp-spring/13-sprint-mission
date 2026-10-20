package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.handler.WebSocketErrorHandler;
import com.sprint.mission.discodeit.security.jwt.JwtAuthenticationChannelInterceptor;
import com.sprint.mission.discodeit.security.role.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
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
    // 메모리 기반 심플 브로커

    // 하트비트를 위한 태스크 스케쥴러
    private TaskScheduler messageBrokerTaskScheduler;
    private final JwtAuthenticationChannelInterceptor authInterceptor;
    private final WebSocketErrorHandler errorHandler;

    private final RoleHierarchy roleHierarchy;

    // postConstructor 이후 태스크 스케쥴러 주입 매서드.
    @Autowired
    public void setMessageBrokerTaskScheduler(@Lazy TaskScheduler messageBrokerTaskScheduler) {
        this.messageBrokerTaskScheduler = messageBrokerTaskScheduler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 스프링 내부 세션에 등록하는 주소
        registry.enableSimpleBroker("/sub")
                .setHeartbeatValue(new long[]{10000, 10000})
                .setTaskScheduler(messageBrokerTaskScheduler);
        // 메세지를 발송하는 경우의 경로
        registry.setApplicationDestinationPrefixes("/pub");

    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws");
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();   // legacy 지원용 설정.
        registry.setErrorHandler(errorHandler);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(
                authInterceptor,
                new SecurityContextChannelInterceptor(),
                authorizationChannelInterceptor()
                );
    }


    private AuthorizationChannelInterceptor authorizationChannelInterceptor() {

        AuthorityAuthorizationManager<MessageAuthorizationContext<?>> userManager =
                AuthorityAuthorizationManager.hasRole(Role.USER.name());

        // Security 설정의 roleHierachy 를 사용하도록 수정
        userManager.setRoleHierarchy(roleHierarchy);

        return new AuthorizationChannelInterceptor(
                MessageMatcherDelegatingAuthorizationManager.builder()
                        // 연결끊기 및 구독 해제는 전원 허용.
                        .simpTypeMatchers(
                                SimpMessageType.DISCONNECT,
                                SimpMessageType.UNSUBSCRIBE
                        ).permitAll()
                        // 나머진 유저권한 확인.
                        .anyMessage().access(userManager)
                        .build()
        );
    }
}
