
package com.sprint.mission.discodeit.websocket;

import com.nimbusds.jwt.JWTClaimsSet;
import com.sprint.mission.discodeit.security.ChannelSecurity;
import com.sprint.mission.discodeit.security.JwtRegistry;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketAuthChannelInterceptorTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtRegistry jwtRegistry;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private ChannelSecurity channelSecurity;

    @Mock
    private MessageChannel messageChannel;

    @InjectMocks
    private WebSocketAuthChannelInterceptor interceptor;

    @Test
    @DisplayName("JWT가 없는 STOMP CONNECT 요청은 거부된다")
    void shouldRejectConnectWithoutJwt() {

        // given
        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(StompCommand.CONNECT);

        accessor.setLeaveMutable(true);

        Message<byte[]> message = MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders()
        );

        // when & then
        assertThatThrownBy(
                () -> interceptor.preSend(message, messageChannel)
        )
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("WebSocket Access Token이 필요합니다.");

        verifyNoInteractions(
                jwtTokenProvider,
                jwtRegistry,
                userDetailsService,
                channelSecurity
        );
    }

    @Test
    @DisplayName("유효한 JWT를 사용한 STOMP CONNECT 요청은 인증에 성공한다")
    void shouldAuthenticateConnectWithValidJwt() {

        // given
        String accessToken = "valid-access-token";
        String username = "testUser";

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(StompCommand.CONNECT);

        accessor.setNativeHeader(
                "Authorization",
                "Bearer " + accessToken
        );

        accessor.setLeaveMutable(true);

        Message<byte[]> message = MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders()
        );

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(username)
                .build();

        when(jwtTokenProvider.parseAccessToken(accessToken))
                .thenReturn(claims);

        when(jwtRegistry.hasActiveJwtInformationByAccessToken(accessToken))
                .thenReturn(true);

        when(jwtTokenProvider.getUsername(claims))
                .thenReturn(username);

        UserDetails userDetails = mock(UserDetails.class);

        when(userDetailsService.loadUserByUsername(username))
                .thenReturn(userDetails);

        doReturn(List.of(
                new SimpleGrantedAuthority("ROLE_USER")
        )).when(userDetails).getAuthorities();

        // when
        Message<?> result =
                interceptor.preSend(message, messageChannel);

        // then
        assertThat(result).isSameAs(message);

        assertThat(accessor.getUser())
                .isInstanceOfSatisfying(
                        UsernamePasswordAuthenticationToken.class,
                        authentication -> {
                            assertThat(authentication.isAuthenticated())
                                    .isTrue();

                            assertThat(authentication.getPrincipal())
                                    .isSameAs(userDetails);

                            assertThat(authentication.getAuthorities())
                                    .extracting("authority")
                                    .contains("ROLE_USER");
                        }
                );

        verify(jwtTokenProvider)
                .parseAccessToken(accessToken);

        verify(jwtRegistry)
                .hasActiveJwtInformationByAccessToken(accessToken);

        verify(jwtTokenProvider)
                .getUsername(claims);

        verify(userDetailsService)
                .loadUserByUsername(username);

        verifyNoInteractions(channelSecurity);
    }


    @Test
    @DisplayName("무효화된 JWT를 사용한 STOMP CONNECT 요청은 거부된다")
    void shouldRejectConnectWithInvalidatedJwt() {
        // given
        String accessToken = "invalidated-access-token";

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(StompCommand.CONNECT);

        accessor.setNativeHeader(
                "Authorization",
                "Bearer " + accessToken
        );
        accessor.setLeaveMutable(true);

        Message<byte[]> message = MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders()
        );

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("testUser")
                .build();

        when(jwtTokenProvider.parseAccessToken(accessToken))
                .thenReturn(claims);

        when(jwtRegistry.hasActiveJwtInformationByAccessToken(accessToken))
                .thenReturn(false);

        // when & then
        assertThatThrownBy(
                () -> interceptor.preSend(message, messageChannel)
        )
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("무효화된 Access Token입니다.");

        verify(jwtTokenProvider).parseAccessToken(accessToken);
        verify(jwtRegistry)
                .hasActiveJwtInformationByAccessToken(accessToken);

        verifyNoInteractions(userDetailsService, channelSecurity);
    }


    @Test
    @DisplayName("개인 채널 비참여자의 STOMP SUBSCRIBE 요청은 거부된다")
    void shouldRejectSubscribeWhenUserIsNotParticipant() {
        // given
        UUID channelId = UUID.randomUUID();

        String destination =
                "/sub/channels." + channelId + ".messages";

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(StompCommand.SUBSCRIBE);

        accessor.setDestination(destination);

        UserDetails userDetails = mock(UserDetails.class);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        accessor.setUser(authentication);
        accessor.setLeaveMutable(true);

        Message<byte[]> message = MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders()
        );

        when(channelSecurity.canAccess(channelId, authentication))
                .thenReturn(false);

        // when & then
        assertThatThrownBy(
                () -> interceptor.preSend(message, messageChannel)
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("해당 채널을 구독할 권한이 없습니다.");

        verify(channelSecurity).canAccess(channelId, authentication);

        verifyNoInteractions(
                jwtTokenProvider,
                jwtRegistry,
                userDetailsService
        );
    }

    @Test
    @DisplayName("허용되지 않은 STOMP SEND 경로는 거부된다")
    void shouldRejectSendToUnauthorizedDestination() {
        // given
        String destination = "/pub/invalid";

        StompHeaderAccessor accessor =
                StompHeaderAccessor.create(StompCommand.SEND);

        accessor.setDestination(destination);

        UserDetails userDetails = mock(UserDetails.class);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        accessor.setUser(authentication);
        accessor.setLeaveMutable(true);

        Message<byte[]> message = MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders()
        );

        // when & then
        assertThatThrownBy(
                () -> interceptor.preSend(message, messageChannel)
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("허용되지 않은 메시지 전송 경로입니다.");

        verifyNoInteractions(
                channelSecurity,
                jwtTokenProvider,
                jwtRegistry,
                userDetailsService
        );
    }




}
