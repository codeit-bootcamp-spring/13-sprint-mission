package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
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

@ExtendWith(MockitoExtension.class)
class DiscodeitUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private DiscodeitUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_사용자_정보를_반환한다() {
        User user = new User(
                "codeit",
                "codeit@example.com",
                "encoded-password"
        );

        UserResponse userResponse = new UserResponse(
                UUID.randomUUID(),
                "codeit",
                "codeit@example.com",
                false,
                null
        );

        given(userRepository.findByUsername("codeit"))
                .willReturn(Optional.of(user));
        given(userMapper.toDto(user)).willReturn(userResponse);

        UserDetails result =
                userDetailsService.loadUserByUsername("codeit");

        assertThat(result.getUsername()).isEqualTo("codeit");
        assertThat(result.getPassword())
                .isEqualTo("encoded-password");
        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void loadUserByUsername_사용자가_없으면_실패한다() {
        given(userRepository.findByUsername("unknown"))
                .willReturn(Optional.empty());

        assertThatThrownBy(
                () -> userDetailsService
                        .loadUserByUsername("unknown")
        )
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }
}