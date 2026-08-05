package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
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
    private UserStatusRepository userStatusRepository;

    @Mock
    private BinaryContentRepository binaryContentRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private BinaryContentStorage binaryContentStorage;

    @InjectMocks
    private BasicUserService userService;

    @Test
    void create_성공() {
        UserRequest request = new UserRequest(
                "codeit",
                "codeit@example.com",
                "password123"
        );
        UserResponse expected = userResponse("codeit", "codeit@example.com");

        given(userRepository.existsByUsername(request.username())).willReturn(false);
        given(userRepository.existsByEmail(request.email())).willReturn(false);
        given(userRepository.save(any(User.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(userMapper.toDto(any(User.class))).willReturn(expected);

        UserResponse actual = userService.create(request, null);

        assertThat(actual).isEqualTo(expected);
        then(userRepository).should().save(any(User.class));
        then(userStatusRepository).should().save(any(UserStatus.class));
        then(binaryContentRepository).should(never()).save(any());
    }

    @Test
    void create_중복된_사용자_이름이면_실패() {
        UserRequest request = new UserRequest(
                "codeit",
                "codeit@example.com",
                "password123"
        );
        given(userRepository.existsByUsername(request.username())).willReturn(true);

        assertThatThrownBy(() -> userService.create(request, null))
                .isInstanceOf(UserAlreadyExistException.class)
                .hasMessage("이미 존재하는 사용자입니다.");

        then(userRepository).should(never()).save(any(User.class));
        then(userStatusRepository).shouldHaveNoInteractions();
    }

    @Test
    void update_성공() {
        UUID userId = UUID.randomUUID();
        User user = new User("before", "before@example.com", "password123");
        UserRequest request = new UserRequest("after", null, null);
        UserResponse expected = userResponse("after", "before@example.com");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userMapper.toDto(user)).willReturn(expected);

        UserResponse actual = userService.update(userId, request, null);

        assertThat(actual).isEqualTo(expected);
        assertThat(user.getUsername()).isEqualTo("after");
        assertThat(user.getEmail()).isEqualTo("before@example.com");
        assertThat(user.getPassword()).isEqualTo("password123");
        then(userMapper).should().toDto(user);
    }

    @Test
    void update_사용자가_없으면_실패() {
        UUID userId = UUID.randomUUID();
        UserRequest request = new UserRequest("after", null, null);
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(userId, request, null))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        then(userMapper).shouldHaveNoInteractions();
    }

    @Test
    void delete_성공() {
        UUID userId = UUID.randomUUID();
        User user = new User("codeit", "codeit@example.com", "password123");
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        userService.delete(userId);

        then(userStatusRepository).should().deleteById(userId);
        then(userRepository).should().delete(user);
        then(binaryContentRepository).should(never()).deleteById(any());
    }

    @Test
    void delete_사용자가_없으면_실패() {
        UUID userId = UUID.randomUUID();
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        then(userStatusRepository).shouldHaveNoInteractions();
        then(userRepository).should(never()).delete(any(User.class));
    }

    private UserResponse userResponse(String username, String email) {
        return new UserResponse(
                UUID.randomUUID(),
                username,
                email,
                false,
                null
        );
    }
}