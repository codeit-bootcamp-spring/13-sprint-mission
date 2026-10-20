
package com.sprint.mission.discodeit.websocket;

import com.sprint.mission.discodeit.dto.request.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.security.ChannelSecurity;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class MessageWebSocketControllerTest {

    @Mock
    private MessageService messageService;

    @Mock
    private ChannelSecurity channelSecurity;

    @InjectMocks
    private MessageWebSocketController controller;

    @Test
    @DisplayName("인증되지 않은 사용자는 WebSocket 메시지를 작성할 수 없다")
    void shouldRejectMessageWhenUnauthenticated() {
        // given
        CreateMessageRequest request = new CreateMessageRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "테스트 메시지"
        );

        Principal principal = null;

        // when & then
        assertThatThrownBy(() -> controller.create(request, principal))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("인증이 필요합니다.");

        verifyNoInteractions(channelSecurity, messageService);
    }


    @Test
    @DisplayName("다른 사용자의 ID로 WebSocket 메시지를 작성할 수 없다")
    void shouldRejectMessageWhenAuthorIdDoesNotMatch() {
        // given
        UUID authenticatedUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();

        CreateMessageRequest request = new CreateMessageRequest(
                otherUserId,
                channelId,
                "다른 사용자 명의의 메시지"
        );

        UserDto userDto = mock(UserDto.class);
        when(userDto.id()).thenReturn(authenticatedUserId);

        DiscodeitUserDetails userDetails =
                new DiscodeitUserDetails(userDto, "password");

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        // when & then
        assertThatThrownBy(
                () -> controller.create(request, authentication)
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage(
                        "다른 사용자의 명의로 메세지를 작성할 수 없습니다."
                );

        verifyNoInteractions(channelSecurity, messageService);
    }

    @Test
    @DisplayName("개인 채널 비참여자는 WebSocket 메시지를 작성할 수 없다")
    void shouldRejectMessageWhenUserCannotAccessChannel() {
        // given
        UUID userId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();

        CreateMessageRequest request = new CreateMessageRequest(
                userId,
                channelId,
                "접근 권한이 없는 채널에 보내는 메시지"
        );

        UserDto userDto = mock(UserDto.class);
        when(userDto.id()).thenReturn(userId);

        DiscodeitUserDetails userDetails =
                new DiscodeitUserDetails(userDto, "password");

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        )
                );


        when(channelSecurity.canAccess(channelId, authentication))
                .thenReturn(false);

        // when & then
        assertThatThrownBy(
                () -> controller.create(request, authentication)
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("해당 채널에 메시지를 작성할 권한이 없습니다.");


        verify(channelSecurity).canAccess(channelId, authentication);

        verifyNoInteractions(messageService);
    }

    @Test
    @DisplayName("인증된 사용자가 접근 가능한 채널에 메시지를 작성하면 저장 서비스가 호출된다")
    void shouldCreateMessageWhenUserHasChannelAccess() {
        // given
        UUID userId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();

        CreateMessageRequest request = new CreateMessageRequest(
                userId,
                channelId,
                "WebSocket 테스트 메시지"
        );

        UserDto userDto = mock(UserDto.class);
        when(userDto.id()).thenReturn(userId);

        DiscodeitUserDetails userDetails =
                new DiscodeitUserDetails(userDto, "password");

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_USER")
                        )
                );
        when(channelSecurity.canAccess(channelId, authentication))
                .thenReturn(true);

        // when
        controller.create(request, authentication);

        // then
        verify(channelSecurity)
                .canAccess(channelId, authentication);

        verify(messageService, times(1))
                .create(
                        eq(request.toCommand()),
                        eq(List.of())
                );
    }


}

