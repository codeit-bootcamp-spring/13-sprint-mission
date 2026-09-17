package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.command.user.UserCreateCommand;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserLoginFailedException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.service.basic.UserReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("DiscodeitUserDetailsService 단위 테스트")
class DiscodeitUserDetailsServiceTest {

    @Mock
    UserReader userReader;

    @Mock
    UserMapper userMapper;

    @InjectMocks
    DiscodeitUserDetailsService userDetailsService;

    @Test
    @DisplayName("사용자명으로 조회한 디스코드잇 사용자를 UserDetails로 변환한다")
    void loadUserByUsername_returnsDiscodeitUserDetails() {
        String username = "testUser";
        String encodedPassword = "$2a$10$encodedPassword";
        User user = new User(
                new UserCreateCommand(username, encodedPassword, "test@example.com"),
                null
        );
        UserDto userDto = userDto(username);
        given(userReader.getByUsername(username)).willReturn(user);
        given(userMapper.toDto(user, true)).willReturn(userDto);

        DiscodeitUserDetails result = (DiscodeitUserDetails) userDetailsService.loadUserByUsername(username);

        assertThat(result.getUserDto()).isSameAs(userDto);
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getPassword()).isEqualTo(encodedPassword);
        then(userReader).should().getByUsername(username);
        then(userMapper).should().toDto(user, true);
    }

    @Test
    @DisplayName("사용자가 존재하지 않으면 UsernameNotFoundException으로 변환한다")
    void loadUserByUsername_throwsUsernameNotFoundException_whenUserDoesNotExist() {
        String username = "unknownUser";
        given(userReader.getByUsername(username)).willThrow(new UserLoginFailedException());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다.")
                .hasCauseInstanceOf(UserLoginFailedException.class);

        then(userReader).should().getByUsername(username);
        then(userMapper).shouldHaveNoInteractions();
    }

    private UserDto userDto(String username) {
        OffsetDateTime now = OffsetDateTime.parse("2026-09-10T10:00:00+09:00");
        return new UserDto(
                UUID.randomUUID(),
                username,
                "test@example.com",
                null,
                true,
                Role.USER,
                now,
                now
        );
    }
}
