package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.command.CreateUserCommand;
import com.sprint.mission.discodeit.dto.command.UpdateUserCommand;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class BasicUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserStatusRepository userStatusRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private BasicUserService userService;

    @Test
    @DisplayName("사용자 생성 성공")
    void create_success() {
        // given
        CreateUserCommand command = new CreateUserCommand(
                "홍길동",
                "hong12@test.com",
                "12345"
                );

        UserDto expected = mock(UserDto.class);

        given(userRepository.existsByUsername("홍길동"))
                .willReturn(false);

        given(userRepository.existsByEmail("hong12@test.com"))
                .willReturn(false);

        given(userMapper.toDto(any(User.class)))
                .willReturn(expected);

        // when
        UserDto result = userService.create(command, null);

        // then
        assertThat(result).isSameAs(expected);

        then(userRepository)
                .should()
                .save(any(User.class));

        then(userStatusRepository)
                .should()
                .save(any(UserStatus.class));

        then(userMapper)
                .should()
                .toDto(any(User.class));
    }

    @Test
    @DisplayName("중복된 사용자 이름이면 예외를 발생")
    void create_fail_duplicateUsername() {
        // given
        CreateUserCommand command = new CreateUserCommand(
                "홍길동",
                "hong12@test.com",
                "12345"
        );

        given(userRepository.existsByUsername("홍길동"))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.create(command, null))
                .isInstanceOf(UserAlreadyExistsException.class);

        then(userRepository)
                .should(never())
                .save(any(User.class));
    }
    
    @Test
    @DisplayName("사용자 수정 성공")
    void update_success() {
        // given
        UUID userId = UUID.randomUUID();

        User user = new User(
                "홍길동",
        "hong12@test.com",
                "12345"
        );

        UpdateUserCommand command = new UpdateUserCommand(
                "홍감자",
                null,
                null,
                null

        );
        UserDto expected = mock(UserDto.class);

        given(userRepository.findById(userId))
        .willReturn(java.util.Optional.of(user));

        given(userRepository.findByUsername("홍감자"))
                .willReturn(Optional.empty());

        given(userMapper.toDto(user))
        .willReturn(expected);

        // when
        UserDto result = userService.update(
                userId,
                command,
                null
        );
    
        // then
        assertThat(result).isSameAs(expected);
        assertThat(user.getUsername()).isEqualTo("홍감자");
        assertThat(user.getEmail()).isEqualTo("hong12@test.com");
        assertThat(user.getPassword()).isEqualTo("12345");

        then(userRepository)
        .should()
        .findById(userId);

        then(userRepository)
                .should()
                .findByUsername("홍감자");

        then(userRepository)
                .should(never())
                .findByEmail(anyString());

        then(userMapper)
        .should()
        .toDto(user);
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 수정할 때 예외 발생")
    void update_fail_userNotFound() {
        //given
        UUID userId = UUID.randomUUID();

        UpdateUserCommand command = new UpdateUserCommand(
                "강호두",
                "hodu3@test.com",
                "778899",
                null
        );

        given(userRepository.findById(userId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.update(userId, command, null))
                .isInstanceOf(UserNotFoundException.class);

        then(userMapper)
                .shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("사용자 삭제 성공")
    void delete_success() {
        // given
        UUID userId = UUID.randomUUID();

        User user = new User(
                "홍길동",
                "hong12@test.com",
                "12345"
        );

        given(userRepository.findById(userId))
                .willReturn(Optional.of(user));

        given(userStatusRepository.findByUserId(userId))
                .willReturn(Optional.empty());

        // when
        userService.delete(userId);

        // then
        then(userRepository)
                .should()
                .delete(user);

    }

    @Test
    @DisplayName("존재하지 않는 사용자 삭제시 예외 발생")
    void delete_fail_userNotFound() {
        // given
        UUID userId = UUID.randomUUID();

        given(userRepository.findById(userId))
                .willReturn(Optional.empty());

        // when & then

        assertThatThrownBy(() -> userService.delete(userId))
                .isInstanceOf(UserNotFoundException.class);

        then(userRepository)
                .should(never())
                .delete(any(User.class));

    }

}
