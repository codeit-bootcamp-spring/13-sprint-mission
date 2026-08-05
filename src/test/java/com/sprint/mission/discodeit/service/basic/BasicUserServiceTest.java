package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.CreateUserRequest;
import com.sprint.mission.discodeit.dto.UpdateUserRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
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
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BasicUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private BasicUserService userService;

    @Nested
    @DisplayName("사용자 생성")
    class CreateTest {

        @Test
        @DisplayName("사용자 생성에 성공한다")
        void createSuccess() {
            // given
            CreateUserRequest request = org.mockito.Mockito.mock(
                    CreateUserRequest.class
            );

            UserDto expectedResponse = org.mockito.Mockito.mock(
                    UserDto.class
            );

            UUID userId = UUID.randomUUID();

            given(request.getUsername()).willReturn("user01");
            given(request.getEmail()).willReturn("user01@example.com");
            given(request.getPassword()).willReturn("password123");

            given(userRepository.existsByUsername("user01"))
                    .willReturn(false);

            given(userRepository.existsByEmail("user01@example.com"))
                    .willReturn(false);

            given(userRepository.save(any(User.class)))
                    .willAnswer(invocation -> {
                        User savedUser = invocation.getArgument(0);
                        return savedUser;
                    });

            given(userMapper.toDto(any(User.class)))
                    .willReturn(expectedResponse);

            // when
            UserDto result = userService.create(request);

            // then
            assertThat(result).isSameAs(expectedResponse);

            then(userRepository)
                    .should()
                    .existsByUsername("user01");

            then(userRepository)
                    .should()
                    .existsByEmail("user01@example.com");

            then(userRepository)
                    .should()
                    .save(any(User.class));

            then(userMapper)
                    .should()
                    .toDto(any(User.class));
        }

        @Test
        @DisplayName("사용자 이름이 중복되면 생성에 실패한다")
        void createFailWhenUsernameAlreadyExists() {
            // given
            CreateUserRequest request = org.mockito.Mockito.mock(
                    CreateUserRequest.class
            );

            given(request.getUsername()).willReturn("duplicate-user");

            given(userRepository.existsByUsername("duplicate-user"))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.create(request))
                    .isInstanceOf(UserAlreadyExistsException.class);

            then(userRepository)
                    .should()
                    .existsByUsername("duplicate-user");

            then(userRepository)
                    .should(never())
                    .save(any(User.class));

            then(userMapper)
                    .shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("이메일이 중복되면 생성에 실패한다")
        void createFailWhenEmailAlreadyExists() {
            // given
            CreateUserRequest request = org.mockito.Mockito.mock(
                    CreateUserRequest.class
            );

            given(request.getUsername()).willReturn("user01");
            given(request.getEmail()).willReturn("duplicate@example.com");

            given(userRepository.existsByUsername("user01"))
                    .willReturn(false);

            given(userRepository.existsByEmail("duplicate@example.com"))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.create(request))
                    .isInstanceOf(UserAlreadyExistsException.class);

            then(userRepository)
                    .should(never())
                    .save(any(User.class));

            then(userMapper)
                    .shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("사용자 수정")
    class UpdateTest {

        @Test
        @DisplayName("사용자 수정에 성공한다")
        void updateSuccess() {
            // given
            UUID userId = UUID.randomUUID();

            UpdateUserRequest request = org.mockito.Mockito.mock(
                    UpdateUserRequest.class
            );

            User user = org.mockito.Mockito.mock(User.class);
            UserDto expectedResponse = org.mockito.Mockito.mock(UserDto.class);

            given(request.getUsername()).willReturn("new-username");
            given(request.getEmail()).willReturn("new@example.com");
            given(request.getPassword()).willReturn("newPassword123");

            given(user.getUsername()).willReturn("old-username");
            given(user.getEmail()).willReturn("old@example.com");

            given(userRepository.findById(userId))
                    .willReturn(Optional.of(user));

            given(userRepository.existsByUsername("new-username"))
                    .willReturn(false);

            given(userRepository.existsByEmail("new@example.com"))
                    .willReturn(false);

            given(userMapper.toDto(user))
                    .willReturn(expectedResponse);

            // when
            UserDto result = userService.update(userId, request);

            // then
            assertThat(result).isSameAs(expectedResponse);

            then(user).should().update(
                    "new-username",
                    "new@example.com",
                    "newPassword123",
                    null
            );

            then(userMapper)
                    .should()
                    .toDto(user);
        }

        @Test
        @DisplayName("존재하지 않는 사용자를 수정하면 실패한다")
        void updateFailWhenUserNotFound() {
            // given
            UUID userId = UUID.randomUUID();

            UpdateUserRequest request = org.mockito.Mockito.mock(
                    UpdateUserRequest.class
            );

            given(userRepository.findById(userId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.update(userId, request))
                    .isInstanceOf(UserNotFoundException.class);

            then(userMapper)
                    .shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("중복된 이메일로 수정하면 실패한다")
        void updateFailWhenEmailAlreadyExists() {
            // given
            UUID userId = UUID.randomUUID();

            UpdateUserRequest request = org.mockito.Mockito.mock(
                    UpdateUserRequest.class
            );

            User user = org.mockito.Mockito.mock(User.class);

            given(request.getUsername()).willReturn("same-username");
            given(request.getEmail()).willReturn("duplicate@example.com");

            given(user.getUsername()).willReturn("same-username");
            given(user.getEmail()).willReturn("old@example.com");

            given(userRepository.findById(userId))
                    .willReturn(Optional.of(user));

            given(userRepository.existsByEmail("duplicate@example.com"))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.update(userId, request))
                    .isInstanceOf(UserAlreadyExistsException.class);

            then(user)
                    .should(never())
                    .update(any(), any(), any(), any());

            then(userMapper)
                    .shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("사용자 삭제")
    class DeleteTest {

        @Test
        @DisplayName("사용자 삭제에 성공한다")
        void deleteSuccess() {
            // given
            UUID userId = UUID.randomUUID();
            User user = org.mockito.Mockito.mock(User.class);

            given(userRepository.findById(userId))
                    .willReturn(Optional.of(user));

            // when
            userService.delete(userId);

            // then
            then(userRepository)
                    .should()
                    .delete(user);
        }

        @Test
        @DisplayName("존재하지 않는 사용자를 삭제하면 실패한다")
        void deleteFailWhenUserNotFound() {
            // given
            UUID userId = UUID.randomUUID();

            given(userRepository.findById(userId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.delete(userId))
                    .isInstanceOf(UserNotFoundException.class);

            then(userRepository)
                    .should(never())
                    .delete(any(User.class));
        }
    }
}