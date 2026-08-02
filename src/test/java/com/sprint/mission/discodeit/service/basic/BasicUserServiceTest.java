package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static org.mockito.ArgumentMatchers.any;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.DuplicateUsernameException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

  @InjectMocks
  private BasicUserService basicUserService;

  @Test
  @DisplayName("유저 생성 성공 - username, email 모두 중복 없으면 생성된다")
  void create_success() {
    // given
    String username = "testuser";
    String email = "test@example.com";
    String password = "password1234!";

    given(userRepository.findByUserName(username)).willReturn(Optional.empty());
    given(userRepository.findByEmail(email)).willReturn(Optional.empty());
    given(userMapper.toDto(org.mockito.ArgumentMatchers.any(User.class)))
        .willReturn(new UserDto(UUID.randomUUID(), username, email, null, false));

    // when
    UserDto result = basicUserService.create(username, email, password, null);

    // then
    assertThat(result.username()).isEqualTo(username);
    assertThat(result.email()).isEqualTo(email);
    verify(userRepository).save(org.mockito.ArgumentMatchers.any(User.class));
  }

  @Test
  @DisplayName("유저 생성 실패 - username이 이미 존재하면 예외가 발생한다")
  void create_fail_duplicateUsername() {
    // given
    String username = "duplicateUser";
    String email = "test@example.com";

    given(userRepository.findByUserName(username))
        .willReturn(Optional.of(new User(username, "pw", email)));

    // when & then
    assertThatThrownBy(() ->
        basicUserService.create(username, email, "password1234!", null))
        .isInstanceOf(DuplicateUsernameException.class);

    verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
  }

  @Test
  @DisplayName("유저 수정 성공 - 존재하는 userId면 정보가 수정된다")
  void update_success() {
    // given
    UUID userId = UUID.randomUUID();
    User existingUser = new User("oldName", "oldPw", "old@example.com");

    given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
    given(userMapper.toDto(existingUser))
        .willReturn(new UserDto(userId, "newName", "new@example.com", null, false));

    // when
    UserDto result = basicUserService.update(userId, "newName", "new@example.com", "newPw1234!",
        null);

    // then
    assertThat(result.username()).isEqualTo("newName");
    assertThat(result.email()).isEqualTo("new@example.com");
  }

  @Test
  @DisplayName("유저 수정 실패 - 존재하지 않는 userId면 예외가 발생한다")
  void update_fail_userNotFound() {
    // given
    UUID userId = UUID.randomUUID();
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() ->
        basicUserService.update(userId, "newName", "new@example.com", "newPw1234!", null))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  @DisplayName("유저 삭제 성공 - 존재하는 userId면 정상 삭제된다")
  void delete_success() {
    // given
    UUID userId = UUID.randomUUID();
    User existingUser = new User("name", "pw", "email@example.com");

    given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));

    // when
    basicUserService.delete(userId);

    // then
    verify(userRepository).deleteById(userId);
  }

  @Test
  @DisplayName("유저 삭제 실패 - 존재하지 않는 userId면 예외가 발생한다")
  void delete_fail_userNotFound() {
    // given
    UUID userId = UUID.randomUUID();
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> basicUserService.delete(userId))
        .isInstanceOf(UserNotFoundException.class);

    verify(userRepository, never()).deleteById(any());
  }
}