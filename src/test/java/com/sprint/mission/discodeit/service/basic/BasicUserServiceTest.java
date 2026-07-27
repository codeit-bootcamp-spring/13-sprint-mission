package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.DuplicateUserException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BasicUserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private BinaryContentRepository binaryContentRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private BinaryContentStorage binaryContentStorage;

  @InjectMocks
  private BasicUserService basicUserService;

  @Test
  @DisplayName("프로필 없이 사용자를 생성할 수 있다.")
  void createWithoutProfile() {
    // given
    UserCreateRequest request = new UserCreateRequest(
        "testUser",
        "test@test.com",
        "password"
    );

    UserDto expected = new UserDto(
        UUID.randomUUID(),
        request.username(),
        request.email(),
        null,
        false
    );

    given(userRepository.findByUsername(request.username()))
        .willReturn(Optional.empty());

    given(userRepository.findByEmail(request.email()))
        .willReturn(Optional.empty());

    given(userMapper.toDto(any(User.class)))
        .willReturn(expected);

    // when
    UserDto result = basicUserService.create(request, null);

    // then
    assertThat(result).isEqualTo(expected);

    then(userRepository).should().save(any(User.class));
    then(userMapper).should().toDto(any(User.class));

    verifyNoInteractions(binaryContentRepository, binaryContentStorage);
  }

  @Test
  @DisplayName("사용자 이름이 중복되면 사용자 생성에 실패한다")
  void createFailsWhenUsernameIsDuplicated() {
    // given
    UserCreateRequest request = new UserCreateRequest(
        "duplicateUser",
        "test@example.com",
        "password"
    );

    User existingUser = new User(
        request.username(),
        "existing@example.com",
        "password",
        null
    );

    given(userRepository.findByUsername(request.username()))
        .willReturn(Optional.of(existingUser));

    // when & then
    assertThatThrownBy(() -> basicUserService.create(request, null))
        .isInstanceOf(DuplicateUserException.class);

    then(userRepository).should(never())
        .save(any(User.class));

    verifyNoInteractions(
        binaryContentRepository,
        binaryContentStorage,
        userMapper
    );
  }

  @Test
  @DisplayName("이메일이 중복되면 사용자 생성에 실패한다")
  void createFailsWhenEmailIsDuplicated() {
    // given
    UserCreateRequest request = new UserCreateRequest(
        "testUser",
        "duplicate@example.com",
        "password"
    );

    User existingUser = new User(
        "existingUser",
        request.email(),
        "password",
        null
    );

    given(userRepository.findByUsername(request.username()))
        .willReturn(Optional.empty());

    given(userRepository.findByEmail(request.email()))
        .willReturn(Optional.of(existingUser));

    // when & then
    assertThatThrownBy(() -> basicUserService.create(request, null))
        .isInstanceOf(DuplicateUserException.class);

    then(userRepository).should(never())
        .save(any(User.class));

    verifyNoInteractions(
        binaryContentRepository,
        binaryContentStorage,
        userMapper
    );
  }

  @Test
  @DisplayName("존재하지 않는 사용자 ID를 조회하면 예외가 발생한다")
  void findByIdFailsWhenUserDoesNotExist() {
    // given
    UUID userId = UUID.randomUUID();

    given(userRepository.findById(userId))
        .willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> basicUserService.findById(userId))
        .isInstanceOf(UserNotFoundException.class);

    then(userMapper).shouldHaveNoInteractions();
  }

}
