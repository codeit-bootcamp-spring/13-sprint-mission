package com.sprint.mission.discodeit.websocket;


import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
import com.sprint.mission.discodeit.security.jwt.registry.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;
    private final UserDetailsService userDetailsService;
    private final WebSocketRegistry webSocketRegistry;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
                StompHeaderAccessor.class);

        if (accessor == null || accessor.getCommand() == null)
            return message;

        switch (accessor.getCommand()) {
            case CONNECT -> authenticate(accessor);
            case SUBSCRIBE -> subscribe(accessor);
        }

        return message;
    }



    private void subscribe(StompHeaderAccessor accessor) {
        Optional.ofNullable(accessor.getDestination())
                .ifPresent(dst -> register(dst,accessor.getSessionId()));
    }

    private void register(String dst, String sessionId){
        Pattern pattern = Pattern.compile("^/sub/channels\\.([0-9a-fA-F-]{36})\\.messages$");
        Matcher matcher = pattern.matcher(dst);
        if (matcher.matches()) {
            String channelId = matcher.group(1);
            webSocketRegistry.register(sessionId, channelId);
            log.debug("[WebSocket] {} 채널 구독됨 - session: {}", channelId, sessionId);
        }
    }



    private void authenticate(StompHeaderAccessor accessor) {
        String token = accessor.getFirstNativeHeader("Authorization");
        if (token != null)
            accessor.setUser(parseToken(token));
    }

    private UsernamePasswordAuthenticationToken parseToken(String preToken) {

        String token = resolveToken(preToken);

        if (jwtTokenProvider.isExpired(token)) {
            log.info("[WebSocket] 만료된 토큰.");
            throw new DiscodeitException(ExceptionCode.EXPIRED_TOKEN, "만료된 토큰 입니다. - %s", token);
        }

        UserDetails auth = userDetailsService.loadUserByUsername(jwtTokenProvider.getSubject(token));

        log.debug("[WebSocket] {} 유저 확인 완료. id : {}",auth.getUsername(), auth.getAuthorities());

        return new UsernamePasswordAuthenticationToken(auth, null, auth.getAuthorities());
    }

    private String resolveToken(String preToken) {
        if(preToken != null && preToken.startsWith("Bearer "))
            return preToken.substring(7);
        else {
            log.debug("[WebSocket] 토큰 형식 에러 : {}", preToken);
            throw new DiscodeitException(ExceptionCode.INCORRECT_CERT_ERROR);
        }
    }

}
