package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNameAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BasicUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReadStatusRepository readStatusRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private BinaryContentRepository binaryContentRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private BasicUserService basicUserService;

    private User sample() {
        return new User("이름_샘플", "이메일_샘플", "패스워드_샘플", null);
    }

    @Nested
    @DisplayName("(생성) create")
    class Create {
        @Test
        @DisplayName("중복된 이름, 이메일이 없으면 유저 생성")
        void 생성_성공() {
            //given
            UserCreateRequest request = new UserCreateRequest(
                    "테스트 이름",
                    "테스트 이메일",
                    "테스트 패스워드"
            );

            User saved = new User(request.username(), request.email(), request.password(), null);

            UserDto createdUserDto = new UserDto(
                    saved.getId(),
                    saved.getUsername(),
                    saved.getEmail(),
                    null,
                    true
            );

            given(userRepository.existsByUsername(request.username()))
                    .willReturn(false);
            given(userRepository.existsByEmail(request.email()))
                    .willReturn(false);
            given(userRepository.save(any(User.class)))
                    .willReturn(saved);
            given(userMapper.toDto(any(User.class)))
                    .willReturn(createdUserDto);

            //when
            UserDto result = basicUserService.createUser(request, null);

            //then
            assertThat(result.username())
                    .isEqualTo(request.username());
            assertThat(result.email())
                    .isEqualTo(request.email());

            then(userRepository).should()
                    .existsByUsername(request.username());
            then(userRepository).should()
                    .existsByEmail(request.email());
            then(userRepository).should()
                    .save(any(User.class));

        }

        @Test
        @DisplayName("중복된 이름이 존재하면 예외 발생")
        void 생성_실패_이름_중복() {
            //given
            UserCreateRequest request = new UserCreateRequest(
                    "테스트 이름",
                    "테스트 이메일",
                    "테스트 패스워드"
            );

            given(userRepository.existsByUsername(request.username()))
                    .willReturn(true);

            //when & then
            assertThatThrownBy(() -> basicUserService.createUser(request, null))
                    .isInstanceOf(UserNameAlreadyExistsException.class);

            then(userRepository).should()
                    .existsByUsername(request.username());
            then(userRepository).should(never())
                    .save(any(User.class));

        }

    }

    @Nested
    @DisplayName("(수정) update")
    class Update {
        @Test
        @DisplayName("사용자가 존재하고 중복이 없으면 정보를 수정")
        void 수정_성공() {
            // given
            UUID randomId = UUID.randomUUID();
            User user = sample();

            UserUpdateRequest request = new UserUpdateRequest(
                    "테스트 이름",
                    "테스트 이메일",
                    "테스트 패스워드"
            );

            UserDto createdUserDto = new UserDto(
                    user.getId(),
                    request.newUsername(),
                    request.newEmail(),
                    null,
                    true
            );

            given(userRepository.findById(randomId))
                    .willReturn(Optional.of(user));
            given(userRepository.existsByUsername(request.newUsername()))
                    .willReturn(false);
            given(userRepository.existsByEmail(request.newEmail()))
                    .willReturn(false);
            given(userMapper.toDto(any(User.class)))
                    .willReturn(createdUserDto);

            // when
            UserDto result = basicUserService.updateUser(randomId, request, null);

            // then
            assertThat(user.getUsername())
                    .isEqualTo(request.newUsername());
            assertThat(user.getEmail())
                    .isEqualTo(request.newEmail());

            assertThat(result.username())
                    .isEqualTo(request.newUsername());
            assertThat(result.email())
                    .isEqualTo(request.newEmail());

            then(userRepository).should().findById(randomId);
        }

        @Test
        @DisplayName("수정할 유저가 존재하지 않으면 예외 발생")
        void 수정_실패_유저_없음() {
            // given
            UUID randomId = UUID.randomUUID();

            UserUpdateRequest request = new UserUpdateRequest(
                    "테스트 이름",
                    "테스트 이메일",
                    "테스트 패스워드"
            );

            given(userRepository.findById(randomId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> basicUserService.updateUser(randomId, request, null))
                    .isInstanceOf(UserNotFoundException.class);

            then(userRepository).should()
                    .findById(randomId);
            then(userRepository).should(never())
                    .existsByUsername(any());
        }
    }

    @Nested
    @DisplayName("(삭제) delete")
    class Delete {
        @Test
        @DisplayName("사용자가 존재하면 삭제")
        void 삭제_성공() {
            // given
            UUID randomId = UUID.randomUUID();
            User user = sample();

            given(userRepository.findById(randomId))
                    .willReturn(Optional.of(user));
            given(readStatusRepository.findAllByUserId(randomId))
                    .willReturn(List.of());
            given(messageRepository.findAllByAuthorId(randomId))
                    .willReturn(List.of());

            // when
            basicUserService.deleteUser(randomId);

            // then
            then(userRepository).should()
                    .findById(randomId);
            then(userRepository).should()
                    .deleteById(randomId);
        }

        @Test
        @DisplayName("삭제할 유저가 존재하지 않으면 예외 발생")
        void 삭제_실패_유저_없음() {
            // given
            UUID randomId = UUID.randomUUID();

            given(userRepository.findById(randomId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> basicUserService.deleteUser(randomId))
                    .isInstanceOf(UserNotFoundException.class);

            then(userRepository).should()
                    .findById(randomId);
            then(userRepository).should(never())
                    .deleteById(any());
        }
    }

}