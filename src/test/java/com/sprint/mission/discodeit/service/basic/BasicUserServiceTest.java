package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
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
import org.mockito.InOrder;
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
  void 프로필_없이_사용자생성() {
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
  void 이름_중복_생성_실패() {
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
  void 이메일_중복_생성_실패() {
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
  void 없는_아이디_예외_발생() {
    // given
    UUID userId = UUID.randomUUID();

    given(userRepository.findById(userId))
        .willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> basicUserService.findById(userId))
        .isInstanceOf(UserNotFoundException.class);

    then(userMapper).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("사용자 정보를 수정할 수 있다")
  void 사용자_정보_수정() {
    // given
    UUID userId = UUID.randomUUID();

    User user = new User(
        "oldUsername",
        "old@test.com",
        "oldPassword",
        null
    );

    UserUpdateRequest request = new UserUpdateRequest(
        "newUsername",
        "new@test.com",
        "newPassword"
    );

    UserDto expected = mock(UserDto.class);

    given(userRepository.findById(userId))
        .willReturn(Optional.of(user));

    given(userRepository.findByUsername(request.newUsername()))
        .willReturn(Optional.empty());

    given(userRepository.findByEmail(request.newEmail()))
        .willReturn(Optional.empty());

    given(userMapper.toDto(user))
        .willReturn(expected);

    // when
    UserDto result =
        basicUserService.update(userId, request, null);

    // then
    assertThat(result).isEqualTo(expected);

    assertThat(user.getUsername())
        .isEqualTo(request.newUsername());

    assertThat(user.getEmail())
        .isEqualTo(request.newEmail());

    assertThat(user.getPassword())
        .isEqualTo(request.newPassword());

    then(userRepository).should()
        .findById(userId);

    then(userRepository).should()
        .findByUsername(request.newUsername());

    then(userRepository).should()
        .findByEmail(request.newEmail());

    then(userMapper).should()
        .toDto(user);

    verifyNoInteractions(
        binaryContentRepository,
        binaryContentStorage
    );
  }

  @Test
  @DisplayName("사용자 삭제 시 프로필 파일도 함께 삭제한다")
  void 사용자_삭제_프로필_삭제() {
    // given
    UUID userId = UUID.randomUUID();
    UUID profileId = UUID.randomUUID();

    User user = mock(User.class);
    BinaryContent profile = mock(BinaryContent.class);

    given(userRepository.findById(userId))
        .willReturn(Optional.of(user));

    given(user.getProfile())
        .willReturn(profile);

    given(profile.getId())
        .willReturn(profileId);

    // when
    basicUserService.delete(userId);

    // then
    then(userRepository).should()
        .findById(userId);

    InOrder inOrder = inOrder(
        binaryContentStorage,
        binaryContentRepository,
        userRepository
    );

    inOrder.verify(binaryContentStorage)
        .delete(profileId);

    inOrder.verify(binaryContentRepository)
        .deleteById(profileId);

    inOrder.verify(userRepository)
        .delete(user);
  }

  @Test
  @DisplayName("존재하지 않는 사용자는 삭제할 수 없다")
  void 없으면_삭제_불가() {
    // given
    UUID userId = UUID.randomUUID();

    given(userRepository.findById(userId))
        .willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(
        () -> basicUserService.delete(userId)
    ).isInstanceOf(UserNotFoundException.class);

    then(userRepository).should()
        .findById(userId);

    then(userRepository).shouldHaveNoMoreInteractions();

    verifyNoInteractions(
        userMapper,
        binaryContentRepository,
        binaryContentStorage
    );
  }

}
