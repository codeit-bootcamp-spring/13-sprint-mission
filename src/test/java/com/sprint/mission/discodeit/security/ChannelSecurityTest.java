package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChannelSecurityTest {

    @Mock
    private ChannelRepository channelRepository;

    @Mock
    private ReadStatusRepository readStatusRepository;

    @InjectMocks
    private ChannelSecurity channelSecurity;
    
    @Test
    @DisplayName("인증되지 않은 사용자는 채널에 접근할 수 없다.")
    void shouldDenyAccessWhenUnauthenticated() {
        UUID channelId = UUID.randomUUID();

        boolean result = channelSecurity.canAccess(channelId, null);

        assertThat(result).isFalse();

        verifyNoInteractions(channelRepository, readStatusRepository);
    }

    @Test
    @DisplayName("인증된 사용자는 공개 채널에 접근할 수 있다")
    void shouldAllowAccessToPublicChannel() {
        // given
        UUID userId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();

        UserDto userDto = mock(UserDto.class);
        when(userDto.id()).thenReturn(userId);

        DiscodeitUserDetails userDetails =
                new DiscodeitUserDetails(userDto, "password");

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        List.of()
                );

        Channel channel = mock(Channel.class);

        when(channelRepository.findById(channelId))
                .thenReturn(Optional.of(channel));

        when(channel.getType()).thenReturn(ChannelType.PUBLIC);

        // when
        boolean result =
                channelSecurity.canAccess(channelId, authentication);

        // then
        assertThat(result).isTrue();

        verify(channelRepository).findById(channelId);
        verifyNoInteractions(readStatusRepository);
    }


    @Test
    @DisplayName("개인 채널 참여자는 해당 채널에 접근할 수 있다")
    void shouldAllowAccessToPrivateChannelWhenParticipant() {
        // given
        UUID userId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();

        UserDto userDto = mock(UserDto.class);
        when(userDto.id()).thenReturn(userId);

        DiscodeitUserDetails userDetails =
                new DiscodeitUserDetails(userDto, "password");

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        List.of()
                );

        Channel channel = mock(Channel.class);
        when(channel.getType()).thenReturn(ChannelType.PRIVATE);

        when(channelRepository.findById(channelId))
                .thenReturn(Optional.of(channel));

        ReadStatus readStatus = mock(ReadStatus.class);

        when(readStatusRepository.findByChannelIdAndUserId(channelId, userId))
                .thenReturn(Optional.of(readStatus));

        // when
        boolean result =
                channelSecurity.canAccess(channelId, authentication);

        // then
        assertThat(result).isTrue();

        verify(readStatusRepository)
                .findByChannelIdAndUserId(channelId, userId);
    }


    @Test
    @DisplayName("개인 채널 비참여자는 해당 채널에 접근할 수 없다")
    void shouldDenyAccessToPrivateChannelWhenNotParticipant() {
        // given
        UUID userId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();

        UserDto userDto = mock(UserDto.class);
        when(userDto.id()).thenReturn(userId);

        DiscodeitUserDetails userDetails =
                new DiscodeitUserDetails(userDto, "password");

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        List.of()
                );

        Channel channel = mock(Channel.class);
        when(channel.getType()).thenReturn(ChannelType.PRIVATE);

        when(channelRepository.findById(channelId))
                .thenReturn(Optional.of(channel));

        when(readStatusRepository.findByChannelIdAndUserId(channelId, userId))
                .thenReturn(Optional.empty());

        // when
        boolean result =
                channelSecurity.canAccess(channelId, authentication);

        // then
        assertThat(result).isFalse();

        verify(readStatusRepository)
                .findByChannelIdAndUserId(channelId, userId);
    }

    @Test
    @DisplayName("존재하지 않는 채널에는 접근할 수 없다")
    void shouldDenyAccessWhenChannelNotFound() {
        // given
        UUID channelId = UUID.randomUUID();

        UserDto userDto = mock(UserDto.class);

        DiscodeitUserDetails userDetails =
                new DiscodeitUserDetails(userDto, "password");

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        List.of()
                );

        when(channelRepository.findById(channelId))
                .thenReturn(Optional.empty());

        // when
        boolean result =
                channelSecurity.canAccess(channelId, authentication);

        // then
        assertThat(result).isFalse();

        verify(channelRepository).findById(channelId);
        verifyNoInteractions(readStatusRepository);
    }

    @Test
    @DisplayName("인증 객체의 Principal이 올바르지 않으면 접근할 수 없다")
    void shouldDenyAccessWhenPrincipalIsInvalid() {
        // given
        UUID channelId = UUID.randomUUID();

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "unknownUser",
                        null,
                        List.of()
                );

        // when
        boolean result =
                channelSecurity.canAccess(channelId, authentication);

        // then
        assertThat(result).isFalse();

        verifyNoInteractions(
                channelRepository,
                readStatusRepository
        );
    }


}
