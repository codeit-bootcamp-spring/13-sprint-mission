package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BasicUserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserStatusRepository userStatusRepository;
    @Mock private BinaryContentRepository binaryContentRepository;
    @Mock private UserMapper userMapper;
    @Mock private BinaryContentStorage binaryContentStorage;

    @InjectMocks private BasicUserService basicUserService;

    @Test
    @DisplayName("사용자 생성 성공")
    void create_success() {
        // given
        UserCreateRequest request = new UserCreateRequest("woody", "woody@test.com", "password123", null);
        given(userRepository.findByUsername("woody")).willReturn(Optional.empty());
        given(userRepository.findByEmail("woody@test.com")).willReturn(Optional.empty());
        given(userMapper.toDto(any(User.class)))
                .willReturn(new UserDto(UUID.randomUUID(), "woody", "woody@test.com", null, false));

        // when
        UserDto result = basicUserService.create(request);

        // then
        assertThat(result.username()).isEqualTo("woody");
        then(userRepository).should().save(any(User.class));
        then(userStatusRepository).should().save(any());
    }

    @Test
    @DisplayName("사용자 생성 실패 - 이름 중복")
    void create_fail_duplicateUsername() {
        // given
        UserCreateRequest request = new UserCreateRequest("woody", "woody@test.com", "password123", null);
        given(userRepository.findByUsername("woody")).willReturn(Optional.of(mock(User.class)));

        // when & then
        assertThatThrownBy(() -> basicUserService.create(request))
                .isInstanceOf(UserAlreadyExistsException.class);

        then(userRepository).should(never()).save(any());
    }
}
