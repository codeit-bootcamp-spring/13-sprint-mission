package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.*;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class) // Mockito 활용해 Repository 의존성 모의
@DisplayName("UserService 슬라이스 테스트 (Mockito)")
public class UserServiceTest { // UserService: create, update, delete 메소드
// 서비스의 핵심 메소드에 대해 각각 최소 2개 이상(성공, 실패)의 테스트 케이스를 작성

  @Mock // 가짜 객체
  UserRepository userRepository;
  @Mock
  BinaryContentRepository contentRepository;
  @Mock
  UserStatusRepository statusRepository;
  @Mock
  UserMapper userMapper;
  @Mock
  BinaryContentStorage storage;

  @InjectMocks
  BasicUserService service; // 가짜 객체 주입받는 서비스 객체

  @Nested
  @DisplayName("생성(create)")
  class Create {

    @Test
    @DisplayName("사용자를 정상적으로 생성한다")
    void 사용자_생성() {
      // given
      UserCreateRequest request = new UserCreateRequest("사용자", "user@icloud.com", "Abcd1234!");
      given(userRepository.existsByEmail(anyString())).willReturn(false);
      given(userRepository.existsByUsername(anyString())).willReturn(false);
      given(userRepository.save(any(User.class))).willAnswer(
          invocation -> invocation.getArgument(0)); // save 메서드 호출될 때 리턴되는 첫 번째 인자값
      given(userMapper.toDto(any(User.class))).willReturn(
          UserDto.builder()
              .id(UUID.randomUUID())
              .username("사용자")
              .email("user@icloud.com")
              .profile(null)
              .online(false)
              .build());

      //when
      service.create(request, Optional.empty());
      ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
      // then
      then(userRepository).should().save(captor.capture());
      User saved = captor.getValue();
      assertAll(
          () -> assertThat(saved.getUsername()).isEqualTo("사용자"),
          () -> assertThat(saved.getEmail()).isEqualTo("user@icloud.com"),
          () -> assertThat(saved.getPassword()).isEqualTo("Abcd1234!")
      );
    }

    @Test
    @DisplayName("기존에 존재하는 이메일 정보로 사용자 생성을 시도하면 실패한다")
    void 중복된_이메일_사용자_생성() {
      // given
      UserCreateRequest request = new UserCreateRequest("사용자", "user@icloud.com", "Abcd1234!");
      given(userRepository.existsByEmail("user@icloud.com")).willReturn(true);
      // when & then
      assertThatThrownBy(() -> service.create(request, Optional.empty())).isInstanceOf(
          UserAlreadyExistsException.class);
      then(userRepository).should().existsByEmail(request.getEmail());
      then(userMapper).should(never()).toDto(any(User.class));
      then(userRepository).should(never()).save(any(User.class));
    }
  }

  @Nested
  @DisplayName("수정(update)")
  class Update {

    @Test
    @DisplayName("사용자 정보를 정상적으로 수정한다")
    void 사용자_수정() {
      // given
      UUID userId = UUID.randomUUID();
      User user = new User("사용자", "user@icloud.com", "Abcd1234!", null);
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(userRepository.existsByEmail(anyString())).willReturn(false);
      given(userRepository.existsByUsername(anyString())).willReturn(false);
      given(userMapper.toDto(any(User.class))).willAnswer(invocation -> {
        User u = invocation.getArgument(0);
        return UserDto.builder()
            .id(userId)
            .username(u.getUsername())
            .email(u.getEmail())
            .profile(null)
            .online(false)
            .build();
      });

      //when
      UserDto result = service.update(userId,
          new UserUpdateRequest("변경된 사용자", "userupdate@icloud.com", "Esdf1234!"),
          Optional.empty());
      // then
      then(userRepository).should().findById(userId);
      then(userRepository).should().existsByUsername("변경된 사용자");
      then(userRepository).should().existsByEmail("userupdate@icloud.com");
      assertAll(
          () -> assertThat(result.getUsername()).isEqualTo("변경된 사용자"),
          () -> assertThat(result.getEmail()).isEqualTo("userupdate@icloud.com")
      );
    }

    @Test
    @DisplayName("이미 사용 중인 이메일 정보로 사용자 수정을 시도하면 실패한다")
    void 중복된_이메일_사용자_수정() {
      // given
      UUID userId = UUID.randomUUID();
      User user = new User("사용자", "user@icloud.com", "Abcd1234!", null);
      ReflectionTestUtils.setField(user, "id", userId);
      UserUpdateRequest request = new UserUpdateRequest("변경된 사용자", "update@icloud.com",
          "Abcd1234!");
      given(userRepository.findById(userId)).willReturn(Optional.of(user));
      given(userRepository.existsByEmail("update@icloud.com")).willReturn(true);
      // when & then
      assertThatThrownBy(
          () -> service.update(userId, request, Optional.empty())).isInstanceOf(
          UserAlreadyExistsException.class);
      then(userMapper).should(never()).toDto(any());
      then(userRepository).should().findById(userId);
      then(userRepository).should().existsByEmail("update@icloud.com");
    }
  }

  @Nested
  @DisplayName("삭제(delete)")
  class Delete {

    @Test
    @DisplayName("사용자를 정상적으로 삭제한다")
    void 사용자_삭제() {
      // given
      UUID userId = UUID.randomUUID();
      given(userRepository.existsById(userId)).willReturn(true);
      //when
      service.delete(userId);
      // then
      then(userRepository).should().existsById(userId);
      then(userRepository).should().deleteById(userId);
    }

    @Test
    @DisplayName("존재하지 않는 정보로 사용자 삭제를 시도하면 실패한다")
    void 존재하지_않는_사용자_삭제() {
      // given
      UUID userId = UUID.randomUUID();
      given(userRepository.existsById(userId)).willReturn(false);
      //when
      assertThatThrownBy(() -> service.delete(userId)).isInstanceOf(UserNotFoundException.class);
      // then
      then(userRepository).should().existsById(userId);
      then(userRepository).should(never()).deleteById(userId);
    }
  }
}
