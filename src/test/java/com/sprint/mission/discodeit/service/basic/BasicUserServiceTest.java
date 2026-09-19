package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicUserService 단위 테스트")
class BasicUserServiceTest {

  @Mock
  UserRepository repository;
  @Mock
  BinaryContentRepository contentRepository;
  @Mock
  BinaryContentStorage storage;
  @Mock
  UserMapper mapper;
  @Mock
  PasswordEncoder passwordEncoder;
  @InjectMocks
  BasicUserService service;

  @Test
  @DisplayName("사용자를 정상적으로 생성한다")
  void 사용자_생성() {
    UserCreateRequest request =
        new UserCreateRequest("password", "김김김", "asdf@test.com", null, Role.USER);
    UserResponse response =
        new UserResponse(java.util.UUID.randomUUID(), "김김김", "asdf@test.com", null, false, Role.USER);
    given(mapper.toDto(any(User.class))).willReturn(response);

    UserResponse result = service.createUser(request, null);

    assertThat(result).isEqualTo(response);
    then(repository).should().save(any(User.class));
  }

  @Test
  @DisplayName("이메일이 중복되면 사용자 생성에 실패한다")
  void 이메일_중복사용자_생성에_실패() {
    UserCreateRequest request =
        new UserCreateRequest("password", "김김김", "asdf@test.com", null, Role.USER);
    given(repository.existsByEmail("asdf@test.com")).willReturn(true);

    assertThatThrownBy(() -> service.createUser(request, null))
        .isInstanceOf(UserAlreadyExistsException.class);
    then(repository).should(never()).save(any());
  }
}
