package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusNotFoundByUserIdException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BasicAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserStatusRepository userStatusRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private BasicAuthService authService;

    @Nested
    @DisplayName("로그인")
    class Login {

        @Test
        @DisplayName("사용자와 사용자 상태가 존재하면 로그인에 성공")
        void login_success() {
            // given
            LoginRequest request = new LoginRequest("user1", "password1");

            UUID userId = UUID.randomUUID();

            User user = new User(
                    "user1",
                    "user1@test.com",
                    "password1",
                    null
            );

            UserStatus userStatus = new UserStatus(user);

            UserDto expectedResponse = createUserDto(userId);

            given(userRepository.findByUsernameAndPassword(
                    request.username(),
                    request.password()
            )).willReturn(Optional.of(user));

            given(userStatusRepository.findByUserId(user.getId()))
                    .willReturn(Optional.of(userStatus));

            given(userMapper.toDto(user))
                    .willReturn(expectedResponse);

            // when
            UserDto result = authService.login(request);

            // then
            assertThat(result)
                    .isEqualTo(expectedResponse);

            then(userRepository).should()
                    .findByUsernameAndPassword(
                            request.username(),
                            request.password()
                    );

            then(userStatusRepository).should()
                    .findByUserId(user.getId());

            then(userMapper).should()
                    .toDto(user);
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 예외가 발생")
        void login_fail_no_user() {
            // given
            LoginRequest request = new LoginRequest("unknown", "wrong-password");

            given(userRepository.findByUsernameAndPassword(
                    request.username(),
                    request.password()
            )).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(UserNotFoundException.class);

            then(userRepository).should()
                    .findByUsernameAndPassword(
                            request.username(),
                            request.password()
                    );
        }

        @Test
        @DisplayName("사용자 상태가 존재하지 않으면 예외가 발생")
        void login_fail_no_userStatus() {
            // given
            LoginRequest request = new LoginRequest("user1", "password1");

            UUID userId = UUID.randomUUID();

            User user = mock(User.class);

            given(user.getId()).willReturn(userId);

            given(userRepository.findByUsernameAndPassword(
                    request.username(),
                    request.password()
            )).willReturn(Optional.of(user));

            given(userStatusRepository.findByUserId(userId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(UserStatusNotFoundByUserIdException.class);

            then(userStatusRepository).should()
                    .findByUserId(userId);
        }
    }

    private UserDto createUserDto(UUID userId) {
         return new UserDto(
                 userId,
                 "user1",
                 "user1@test.com",
                 null,
                 true
         );
    }

}