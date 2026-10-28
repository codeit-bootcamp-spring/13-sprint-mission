package com.sprint.mission.discodeit.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * CONNECT/STOMP 프레임의 Authorization 헤더로 WebSocket 세션을 인증한다.
 * 토큰 유효성, ACCESS 유형, 레지스트리 등록 여부를 확인하고 현재 사용자 권한을 조회한다.
 * 인증 결과는 프레임의 user에 저장하고 이후 SecurityContext 설정은 다음 인터셉터에 맡긴다.
 */
@Component
@RequiredArgsConstructor
public class JwtStompAuthenticationInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;
    private final DiscodeitUserDetailsService userDetailsService;

    @Override
    public @Nullable Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null ) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (command != StompCommand.CONNECT && command != StompCommand.STOMP){
            return message;
        }

        String authorization = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BadCredentialsException("인증 토큰이 필요합니다.");
        }

        String accessToken = authorization.substring(BEARER_PREFIX.length());
        Claims claims = jwtTokenProvider.validateToken(accessToken)
                .filter(value -> JwtTokenProvider.TokenType.ACCESS.name().equals(jwtTokenProvider.getTokenType(value)))
                .filter(value -> jwtRegistry.hasActiveJwtInformationByAccessToken(accessToken))
                .orElseThrow(() -> new BadCredentialsException("유효하지 않은 액세스 토큰입니다."));

        DiscodeitUserDetails userDetails = userDetailsService.loadUserById(jwtTokenProvider.getUserId(claims));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        // 프레임/세션에 인증을 연결해야 비동기 메시지 처리에서도 다음 인터셉터가 인증 정보를 복원한다.
        accessor.setUser(authentication);

        return message;
    }
}
