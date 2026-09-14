package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("DiscodeitUserDetailsService 테스트")
class DiscodeitUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private DiscodeitUserDetailsService userDetailsService;

    @Nested
    @DisplayName("사용자 조회")
    class LoadUserByUsername {

        @Test
        @DisplayName("사용자 이름이 존재하면 UserDetails를 반환")
        void load_success() {
            // given
            String username = "user1";
            String encodedPassword = "encoded-password";
            UUID userId = UUID.randomUUID();

            User user = new User(
                    username,
                    "user1@test.com",
                    encodedPassword,
                    null,
                    Role.USER
            );

            UserDto userDto = new UserDto(
                    userId,
                    username,
                    "user1@test.com",
                    null,
                    true,
                    Role.USER
            );

            given(userRepository.findByUsername(username))
                    .willReturn(Optional.of(user));

            given(userMapper.toDto(user, true))
                    .willReturn(userDto);

            // when
            UserDetails result =
                    userDetailsService.loadUserByUsername(username);

            // then
            assertThat(result)
                    .isInstanceOf(DiscodeitUserDetails.class);

            assertThat(result.getUsername())
                    .isEqualTo(username);

            assertThat(result.getPassword())
                    .isEqualTo(encodedPassword);

            assertThat(result.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_USER");

            DiscodeitUserDetails discodeitUserDetails =
                    (DiscodeitUserDetails) result;

            assertThat(discodeitUserDetails.getUserDto())
                    .isEqualTo(userDto);

            then(userRepository).should()
                    .findByUsername(username);

            then(userMapper).should()
                    .toDto(user, true);
        }

        @Test
        @DisplayName("사용자 이름이 존재하지 않으면 UsernameNotFoundException 발생")
        void load_fail() {
            // given
            String username = "unknown";

            given(userRepository.findByUsername(username))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                    () -> userDetailsService.loadUserByUsername(username)
            )
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining(username);

            then(userRepository).should()
                    .findByUsername(username);

            then(userMapper).shouldHaveNoInteractions();
        }
    }
}