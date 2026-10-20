
package com.sprint.mission.discodeit.websocket;

import com.nimbusds.jwt.JWTClaimsSet;
import com.sprint.mission.discodeit.security.ChannelSecurity;
import com.sprint.mission.discodeit.security.JwtRegistry;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String SUB_PREFIX = "/sub/channels.";
    private static final String SUB_SUFFIX = ".messages";

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;
    private final UserDetailsService userDetailsService;
    private final ChannelSecurity channelSecurity;

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );

        if (accessor == null) {
            throw new AccessDeniedException(
                    "STOMP 메시지 정보를 확인할 수 없습니다."
            );
        }

        StompCommand command = accessor.getCommand();

        if (command == StompCommand.CONNECT) {
            authenticateConnect(accessor);
        } else if (command == StompCommand.SUBSCRIBE) {
            validateSubscribe(accessor);
        } else if (command == StompCommand.SEND) {
            validateSend(accessor);
        }

        return message;
    }

    private void authenticateConnect(StompHeaderAccessor accessor) {

        String authorization =
                accessor.getFirstNativeHeader("Authorization");

        if (authorization == null
                || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BadCredentialsException(
                    "WebSocket Access Token이 필요합니다."
            );
        }

        String accessToken =
                authorization.substring(BEARER_PREFIX.length()).trim();

        if (accessToken.isBlank()) {
            throw new BadCredentialsException(
                    "Access Token이 비어 있습니다."
            );
        }

        try {
            JWTClaimsSet claims =
                    jwtTokenProvider.parseAccessToken(accessToken);

            if (!jwtRegistry.hasActiveJwtInformationByAccessToken(
                    accessToken
            )) {
                throw new BadCredentialsException(
                        "무효화된 Access Token입니다."
                );
            }

            String username = jwtTokenProvider.getUsername(claims);

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            accessor.setUser(authentication);

        } catch (IllegalArgumentException e) {
            throw new BadCredentialsException(
                    "유효하지 않은 WebSocket Access Token입니다.",
                    e
            );
        }
    }

    private void validateSubscribe(StompHeaderAccessor accessor) {

        Authentication authentication =
                requireAuthentication(accessor);

        String destination = accessor.getDestination();

        if (destination == null
                || !destination.startsWith(SUB_PREFIX)
                || !destination.endsWith(SUB_SUFFIX)) {
            throw new AccessDeniedException(
                    "허용되지 않은 구독 경로입니다."
            );
        }

        String channelIdValue = destination.substring(
                SUB_PREFIX.length(),
                destination.length() - SUB_SUFFIX.length()
        );

        UUID channelId;

        try {
            channelId = UUID.fromString(channelIdValue);
        } catch (IllegalArgumentException e) {
            throw new AccessDeniedException(
                    "채널 ID 형식이 올바르지 않습니다."
            );
        }

        if (!channelSecurity.canAccess(
                channelId,
                authentication
        )) {
            throw new AccessDeniedException(
                    "해당 채널을 구독할 권한이 없습니다."
            );
        }
    }

    private void validateSend(StompHeaderAccessor accessor) {

        requireAuthentication(accessor);

        if (!"/pub/messages".equals(accessor.getDestination())) {
            throw new AccessDeniedException(
                    "허용되지 않은 메시지 전송 경로입니다."
            );
        }
    }

    private Authentication requireAuthentication(
            StompHeaderAccessor accessor
    ) {
        if (!(accessor.getUser()
                instanceof Authentication authentication)
                || !authentication.isAuthenticated()) {
            throw new AccessDeniedException(
                    "인증이 필요합니다."
            );
        }

        return authentication;
    }
}
