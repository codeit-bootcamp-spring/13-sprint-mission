package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.NoSuchElementException;
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
    private UserMapper userMapper;

    @Mock
    private BinaryContentRepository binaryContentRepository;

    @Mock
    private BinaryContentStorage binaryContentStorage;

    /*
      @InjectMocks: 테스트할 실제 객체 생성
      위의 @Mock 객체들을 자동으로 주입
    */
    @InjectMocks
    private BasicUserService userService;


    @Test
    @DisplayName("사용자 생성 성공 - 정상적인 요청이면 UserDto를 반환한다")
    void create_성공() {
        UserCreateRequest request = new UserCreateRequest(
                "testuser", "test@test.com", "password1234"
        );
        User user = new User("testuser", "test@test.com", "password1234", null);
        UserDto expectedDto = new UserDto(
                user.getId(), "testuser", "test@test.com", null, false
        );

        given(userRepository.existsByEmail("test@test.com")).willReturn(false);
        given(userRepository.existsByUsername("testuser")).willReturn(false);
        // userMapper.toDto()가 호출되면 expectedDto 반환
        given(userMapper.toDto(any(User.class))).willReturn(expectedDto);

        // when: 이 메서드를 실행하면
        UserDto result = userService.create(request, Optional.empty());

        // then: 이런 결과가 나와야 함
        assertThat(result.username()).isEqualTo("testuser");
        assertThat(result.email()).isEqualTo("test@test.com");
        // userRepository.save()가 1번 호출됐는지 검증
        then(userRepository).should().save(any(User.class));
    }

    @Test
    @DisplayName("사용자 생성 실패 - 이메일 중복이면 UserAlreadyExistsException 발생")
    void create_실패_이메일중복() {
        // given: 이미 존재하는 이메일
        UserCreateRequest request = new UserCreateRequest(
                "testuser", "duplicate@test.com", "password1234"
        );
        given(userRepository.existsByEmail("duplicate@test.com")).willReturn(true);

        // when & then: 예외가 발생해야 함
        assertThatThrownBy(() -> userService.create(request, Optional.empty()))
                .isInstanceOf(UserAlreadyExistsException.class);

        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 생성 실패 - 사용자명 중복이면 UserAlreadyExistsException 발생")
    void create_실패_사용자명중복() {
        // given: 이메일은 괜찮지만 사용자명이 이미 존재
        UserCreateRequest request = new UserCreateRequest(
                "duplicateuser", "new@test.com", "password1234"
        );
        given(userRepository.existsByEmail("new@test.com")).willReturn(false);
        given(userRepository.existsByUsername("duplicateuser")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.create(request, Optional.empty()))
                .isInstanceOf(UserAlreadyExistsException.class);

        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 수정 성공 - 존재하는 사용자면 수정된 UserDto를 반환한다")
    void update_성공() {
        // given
        UUID userId = UUID.randomUUID();
        User existingUser = new User("olduser", "old@test.com", "oldpassword", null);
        UserUpdateRequest request = new UserUpdateRequest(
                "newuser", "new@test.com", "newpassword"
        );
        UserDto expectedDto = new UserDto(userId, "newuser", "new@test.com", null, false);

        given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
        given(userRepository.existsByEmail("new@test.com")).willReturn(false);
        given(userRepository.existsByUsername("newuser")).willReturn(false);
        given(userMapper.toDto(any(User.class))).willReturn(expectedDto);

        // when
        UserDto result = userService.update(userId, request, Optional.empty());

        // then
        assertThat(result.username()).isEqualTo("newuser");
        assertThat(result.email()).isEqualTo("new@test.com");
    }

    @Test
    @DisplayName("사용자 수정 실패 - 존재하지 않는 userId면 예외 발생")
    void update_실패_사용자없음() {
        // given: DB에 해당 userId가 없는 상황
        UUID notExistUserId = UUID.randomUUID();
        UserUpdateRequest request = new UserUpdateRequest(
                "newuser", "new@test.com", "newpassword"
        );
        given(userRepository.findById(notExistUserId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.update(notExistUserId, request, Optional.empty()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("사용자 수정 실패 - 수정할 이메일이 이미 존재하면 예외 발생")
    void update_실패_이메일중복() {
        // given
        UUID userId = UUID.randomUUID();
        User existingUser = new User("testuser", "test@test.com", "password", null);
        UserUpdateRequest request = new UserUpdateRequest(
                "newuser", "duplicate@test.com", "newpassword"
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
        given(userRepository.existsByEmail("duplicate@test.com")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.update(userId, request, Optional.empty()))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    @DisplayName("사용자 삭제 성공 - 존재하는 사용자면 deleteById가 호출된다")
    void delete_성공() {
        // given: 해당 userId가 존재하는 상황
        UUID userId = UUID.randomUUID();
        given(userRepository.existsById(userId)).willReturn(true);

        // when
        userService.delete(userId);

        // then: deleteById가 정확히 1번 호출됐는지 검증
        then(userRepository).should().deleteById(userId);
    }

    @Test
    @DisplayName("사용자 삭제 실패 - 존재하지 않는 userId면 UserNotFoundException 발생")
    void delete_실패_사용자없음() {
        // given: 해당 userId가 없는 상황
        UUID notExistUserId = UUID.randomUUID();
        given(userRepository.existsById(notExistUserId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.delete(notExistUserId))
                .isInstanceOf(UserNotFoundException.class);

        then(userRepository).should(never()).deleteById(any());
    }
}