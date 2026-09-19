package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @InjectMocks
  private BasicUserService userService;

  @Mock
  private UserRepository userRepository;
  @Mock
  private UserStatusRepository userStatusRepository;
  @Mock
  private BinaryContentStorage binaryContentStorage;
  @Mock
  private UserMapper userMapper;
  @Mock
  private PasswordEncoder passwordEncoder;

  @Test
  @DisplayName("create - 성공: 새로운 유저를 정상적으로 생성")
  void create_Success() {
    // Given
    String email = "test@test.com";
    String username = "tester";
    String password = "password123";

    given(userRepository.existsByUsername(username)).willReturn(false);
    given(userRepository.existsByEmail(email)).willReturn(false);
    given(passwordEncoder.encode(password)).willReturn("encodedPassword");

    User mockUser = new User(email, username, password);

    UserDto mockUserDto = new UserDto(UUID.randomUUID(), username, email, null, true);

    given(userMapper.toDto(any(User.class))).willReturn(mockUserDto);

    // When
    UserDto result = userService.create(email, username, password, null);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.username()).isEqualTo(username);
    assertThat(result.email()).isEqualTo(email);

    then(userRepository).should().save(any(User.class));
    then(userStatusRepository).should().save(any());
  }

  @Test
  @DisplayName("create - 실패: 이미 존재하는 username이면 예외가 발생")
  void create_Fail_DuplicateUsername() {
    // Given
    String username = "duplicateUser";
    given(userRepository.existsByUsername(username)).willReturn(true);

    // When & Then
    assertThatThrownBy(() -> userService.create("test@test.com", username, "pass", null))
        .isInstanceOf(UserAlreadyExistsException.class);

    then(userRepository).should(never()).save(any());
  }

  @Test
  @DisplayName("update - 성공: 유저 정보를 정상적으로 수정")
  void update_Success() {
    // Given
    UUID userId = UUID.randomUUID();
    User existingUser = new User("old@test.com", "oldUser", "oldPass");
    ReflectionTestUtils.setField(existingUser, "id", userId);

    String newEmail = "new@test.com";
    String newUsername = "newUser";

    UserDto updatedDto = new UserDto(userId, newUsername, newEmail, null, true);

    given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
    given(userMapper.toDto(any(User.class))).willReturn(updatedDto);

    // When
    UserDto result = userService.update(userId, newEmail, newUsername,
        "newPass", "Hello", null);

    // Then
    assertThat(result.username()).isEqualTo(newUsername);
    assertThat(result.email()).isEqualTo(newEmail);
    assertThat(existingUser.getUsername()).isEqualTo(newUsername);
  }

  @Test
  @DisplayName("update - 실패: 존재하지 않는 유저를 수정하려 하면 예외가 발생")
  void update_Fail_UserNotFound() {
    // Given
    UUID nonExistentUserId = UUID.randomUUID();
    given(userRepository.findById(nonExistentUserId)).willReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> userService.update(nonExistentUserId,
        "a", "b", "c", "d", null))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  @DisplayName("delete - 성공: 유저를 정상적으로 삭제")
  void delete_Success() {
    // Given
    UUID userId = UUID.randomUUID();
    User existingUser = new User("test@test.com", "tester", "pass");
    ReflectionTestUtils.setField(existingUser, "id", userId);

    given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));

    // When
    userService.delete(userId);

    // Then
    then(userStatusRepository).should().deleteByUserId(userId);
    then(userRepository).should().delete(existingUser);
  }

  @Test
  @DisplayName("delete - 실패: 존재하지 않는 유저를 삭제하려 하면 예외가 발생")
  void delete_Fail_UserNotFound() {
    // Given
    UUID nonExistentUserId = UUID.randomUUID();
    given(userRepository.findById(nonExistentUserId)).willReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> userService.delete(nonExistentUserId))
        .isInstanceOf(UserNotFoundException.class);

    then(userRepository).should(never()).delete(any());
  }
}