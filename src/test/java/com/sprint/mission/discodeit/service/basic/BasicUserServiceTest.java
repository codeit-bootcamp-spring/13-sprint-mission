package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.command.user.UserCreateCommand;
import com.sprint.mission.discodeit.dto.command.user.UserUpdateCommand;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class BasicUserServiceTest {

    // 가짜 의존성 주입

    @Mock private UserRepository userRepository;
    @Mock private UserStatusRepository userStatusRepository;
    @Mock private BinaryContentRepository binaryContentRepository;
    @Mock private ReadStatusRepository readStatusRepository;
    @Mock private UserMapper userMapper;
    @Mock private BinaryContentStorage  binaryContentStorage;

    // 위의 @Mock들이 이 안에 자동으로 주입
    @InjectMocks private BasicUserService userService;

    @Test
    @DisplayName("사용자 생성 성공")
    void 유저생성_성공() {
        // given
        UserCreateCommand command = new UserCreateCommand("박경석", "park@gmail.com", "0000");

        given(userRepository.existsByUsername("박경석")).willReturn(false);
        given(userRepository.existsByEmail("park@gmail.com")).willReturn(false);
        UserDto expected = new UserDto(null, "박경석", "park@gmail.com", null, false);
        given(userMapper.toDto(any(User.class), any(UserStatus.class))).willReturn(expected);

        // when
        UserDto result = userService.createUser(command, null);

        // then
        assertThat(result.username()).isEqualTo("박경석");
        assertThat(result.email()).isEqualTo("park@gmail.com");

        then(userRepository).should().save(any(User.class));
        then(userStatusRepository).should().save(any(UserStatus.class));
    }

    @Test
    @DisplayName("중복된 이름으로 생성 시 실패")
    void 유저생성_중복이름_실패() {
        // given
        UserCreateCommand command = new UserCreateCommand("박경석", "park@gmail.com", "0000");
        given(userRepository.existsByUsername("박경석")).willReturn(true);
        // when & then
        assertThatThrownBy(() -> userService.createUser(command, null))
                .isInstanceOf(UserAlreadyExistsException.class);

    }

    @Test
    @DisplayName("사용자 이름 수정 성공")
    void 유저_수정_성공() {
        // given
        UUID userId = UUID.randomUUID();
        User user = new User("박경석", "park@gmail.com", "0000", null, null);
        UserUpdateCommand command = new UserUpdateCommand("김철수", null, null);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.existsByUsername("김철수")).willReturn(false);
        given(userStatusRepository.findByUserId(userId)).willReturn(Optional.empty());

        UserDto expected = new UserDto(null, "김철수", "park@gmail.com", null, false);
        given(userMapper.toDto(any(User.class), any())).willReturn(expected);

        // when
        UserDto result = userService.updateUser(userId, command, null);

        // then
        assertThat(result.username()).isEqualTo("김철수");
        then(userRepository).should().save(any(User.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 수정 실패")
    void 유저수정_실패() {
        // given
        UUID userId = UUID.randomUUID();
        UserUpdateCommand command = new UserUpdateCommand("김철수", null, null);
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateUser(userId, command, null))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("사용자 삭제 성공")
    void 유저삭제_성공() {
        // given
        UUID userId = UUID.randomUUID();
        User user = new User("박경석", "park@gmail.com", "0000", null, null);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        userService.deleteUser(userId);

        // then
        then(userRepository).should().deleteById(userId);

    }



}