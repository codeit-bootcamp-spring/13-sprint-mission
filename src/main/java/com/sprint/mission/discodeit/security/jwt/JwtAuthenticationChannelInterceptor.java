package com.sprint.mission.discodeit.security.jwt;


import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,
                StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            /*
            커넥션연결
            Authorization:Bearer -> 리프레시 토큰 포함
             */
            // Refresh Token
            String token = accessor.getFirstNativeHeader("Authorization");

            log.debug("[WebSocket] {} 요청",accessor.getCommand());

            if (token != null)
                accessor.setUser(parseToken(token));
        }
        return message;
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
