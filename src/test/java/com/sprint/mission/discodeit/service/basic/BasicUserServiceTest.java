package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.DuplicateUserException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
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
    private UserMapper userMapper;

    @Mock
    private BinaryContentService binaryContentService;

    @InjectMocks
    private BasicUserService userService;

    @Test
    @DisplayName("사용자를 생성한다")
    void create_success() {
        UserCreateRequest request = new UserCreateRequest(
                "tester",
                "tester@example.com",
                "password",
                null,
                null,
                null
        );
        UserDto expected = new UserDto(UUID.randomUUID(), "tester", "tester@example.com", null, true);

        given(userRepository.findByName("tester")).willReturn(Optional.empty());
        given(userRepository.findByEmail("tester@example.com")).willReturn(Optional.empty());
        given(userMapper.toDto(any(User.class), any(UserStatus.class))).willReturn(expected);

        UserDto result = userService.create(request);

        assertThat(result).isEqualTo(expected);
        then(userRepository).should().save(any(User.class));
        then(userStatusRepository).should().save(any(UserStatus.class));
    }

    @Test
    @DisplayName("이미 존재하는 username이면 사용자 생성에 실패한다")
    void create_fail_duplicateUsername() {
        UserCreateRequest request = new UserCreateRequest(
                "tester",
                "tester@example.com",
                "password",
                null,
                null,
                null
        );
        User existingUser = new User("tester", "other@example.com", "password", null);

        given(userRepository.findByName("tester")).willReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(DuplicateUserException.class);

        then(userRepository).should(never()).save(any(User.class));
        then(userStatusRepository).should(never()).save(any(UserStatus.class));
    }

    @Test
    @DisplayName("사용자를 수정한다")
    void update_success() {
        UUID userId = UUID.randomUUID();
        User user = new User("tester", "tester@example.com", "password", null);
        UserStatus userStatus = new UserStatus(user, Instant.now());
        UserUpdateRequest request = new UserUpdateRequest(
                "updated",
                "updated@example.com",
                "newPassword",
                null,
                null,
                null
        );
        UserDto expected = new UserDto(userId, "updated", "updated@example.com", null, true);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.findByName("updated")).willReturn(Optional.empty());
        given(userRepository.findByEmail("updated@example.com")).willReturn(Optional.empty());
        given(userStatusRepository.findByUserId(userId)).willReturn(Optional.of(userStatus));
        given(userMapper.toDto(user, userStatus)).willReturn(expected);

        UserDto result = userService.update(userId, request);

        assertThat(result).isEqualTo(expected);
        assertThat(user.getName()).isEqualTo("updated");
        assertThat(user.getEmail()).isEqualTo("updated@example.com");
        assertThat(user.getPassword()).isEqualTo("newPassword");
    }

    @Test
    @DisplayName("존재하지 않는 사용자는 수정에 실패한다")
    void update_fail_userNotFound() {
        UUID userId = UUID.randomUUID();
        UserUpdateRequest request = new UserUpdateRequest(
                "updated",
                "updated@example.com",
                "newPassword",
                null,
                null,
                null
        );

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(userId, request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("사용자를 삭제한다")
    void delete_success() {
        UUID userId = UUID.randomUUID();
        BinaryContent profile = new BinaryContent("profile.png", "image/png", 10L);
        User user = new User("tester", "tester@example.com", "password", profile);
        UserStatus userStatus = new UserStatus(user, Instant.now());

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userStatusRepository.findByUserId(userId)).willReturn(Optional.of(userStatus));

        userService.delete(userId);

        then(binaryContentService).should().delete(profile.getId());
        then(userStatusRepository).should().delete(userStatus);
        then(userRepository).should().delete(user);
    }

    @Test
    @DisplayName("존재하지 않는 사용자는 삭제에 실패한다")
    void delete_fail_userNotFound() {
        UUID userId = UUID.randomUUID();

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(userId))
                .isInstanceOf(UserNotFoundException.class);

        then(userRepository).should(never()).delete(any(User.class));
    }
}