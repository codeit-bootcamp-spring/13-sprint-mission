package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.auth.LoginRequest;
import com.sprint.mission.discodeit.dto.auth.LoginResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.InvalidCredentialsException;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BasicAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserStatusRepository userStatusRepository;

    @InjectMocks
    private BasicAuthService authService;

    @Test
    @DisplayName("로그인에 성공한다")
    void login_success() {
        LoginRequest request = new LoginRequest("tester", "password");
        BinaryContent profile = new BinaryContent("profile.png", "image/png", 10L);
        User user = new User("tester", "tester@example.com", "password", profile);
        UserStatus userStatus = new UserStatus(user, Instant.now());

        given(userRepository.findByName("tester")).willReturn(Optional.of(user));
        given(userStatusRepository.findByUserId(user.getId())).willReturn(Optional.of(userStatus));

        LoginResponse result = authService.login(request);

        assertThat(result.id()).isEqualTo(user.getId());
        assertThat(result.username()).isEqualTo("tester");
        assertThat(result.email()).isEqualTo("tester@example.com");
        assertThat(result.profileId()).isEqualTo(profile.getId());
        assertThat(result.isOnline()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 username이면 로그인에 실패한다")
    void login_fail_userNotFound() {
        LoginRequest request = new LoginRequest("unknown", "password");

        given(userRepository.findByName("unknown")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 로그인에 실패한다")
    void login_fail_wrongPassword() {
        LoginRequest request = new LoginRequest("tester", "wrong-password");
        User user = new User("tester", "tester@example.com", "password", null);

        given(userRepository.findByName("tester")).willReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("사용자 상태가 없으면 offline으로 로그인 응답을 반환한다")
    void login_success_withoutUserStatus() {
        LoginRequest request = new LoginRequest("tester", "password");
        User user = new User("tester", "tester@example.com", "password", null);

        given(userRepository.findByName("tester")).willReturn(Optional.of(user));
        given(userStatusRepository.findByUserId(user.getId())).willReturn(Optional.empty());

        LoginResponse result = authService.login(request);

        assertThat(result.id()).isEqualTo(user.getId());
        assertThat(result.profileId()).isNull();
        assertThat(result.isOnline()).isFalse();
    }
}