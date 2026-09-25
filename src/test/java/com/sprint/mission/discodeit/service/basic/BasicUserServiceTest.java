package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNameAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.JwtRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtRegistry jwtRegistry;

    @InjectMocks
    private BasicUserService basicUserService;

    private User sample() {
        return new User(
                "이름_샘플",
                "이메일_샘플",
                "패스워드_샘플",
                null,
                Role.USER
        );
    }

    @Nested
    @DisplayName("(생성) create")
    class Create {

        @Test
        @DisplayName("중복된 이름, 이메일이 없으면 유저 생성")
        void create_success() {
            // given
            UserCreateRequest request = new UserCreateRequest(
                    "테스트 이름",
                    "테스트 이메일",
                    "테스트 패스워드"
            );

            String encodedPassword = "암호화된 패스워드";

            User saved = new User(
                    request.username(),
                    request.email(),
                    encodedPassword,
                    null,
                    Role.USER
            );

            UserDto createdUserDto = new UserDto(
                    saved.getId(),
                    saved.getUsername(),
                    saved.getEmail(),
                    null,
                    false,
                    Role.USER
            );

            given(userRepository.existsByUsername(request.username()))
                    .willReturn(false);

            given(userRepository.existsByEmail(request.email()))
                    .willReturn(false);

            given(passwordEncoder.encode(request.password()))
                    .willReturn(encodedPassword);

            given(userRepository.save(any(User.class)))
                    .willReturn(saved);

            given(userMapper.toDto(any(User.class), eq(false)))
                    .willReturn(createdUserDto);

            // when
            UserDto result =
                    basicUserService.createUser(request, null);

            // then
            assertThat(result.username())
                    .isEqualTo(request.username());

            assertThat(result.email())
                    .isEqualTo(request.email());

            assertThat(result.online())
                    .isFalse();

            assertThat(result.role())
                    .isEqualTo(Role.USER);

            then(userRepository).should()
                    .existsByUsername(request.username());

            then(userRepository).should()
                    .existsByEmail(request.email());

            then(passwordEncoder).should()
                    .encode(request.password());

            then(userRepository).should()
                    .save(any(User.class));

            then(userMapper).should()
                    .toDto(any(User.class), eq(false));
        }

        @Test
        @DisplayName("중복된 이름이 존재하면 예외 발생")
        void create_fail() {
            // given
            UserCreateRequest request = new UserCreateRequest(
                    "테스트 이름",
                    "테스트 이메일",
                    "테스트 패스워드"
            );

            given(userRepository.existsByUsername(request.username()))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(
                    () -> basicUserService.createUser(request, null)
            ).isInstanceOf(
                    UserNameAlreadyExistsException.class
            );

            then(userRepository).should()
                    .existsByUsername(request.username());

            then(userRepository).should(never())
                    .save(any(User.class));

            then(passwordEncoder).should(never())
                    .encode(any());
        }
    }

    @Nested
    @DisplayName("(조회) get")
    class Get {

        @Test
        @DisplayName("활성 JWT 정보가 없으면 offline 상태로 유저 조회")
        void getUser_offline() {
            // given
            UUID userId = UUID.randomUUID();

            User user = sample();

            UserDto userDto = new UserDto(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    null,
                    false,
                    Role.USER
            );

            given(userRepository.findById(userId))
                    .willReturn(Optional.of(user));

            given(jwtRegistry.hasActiveJwtInformationByUserId(userId))
                    .willReturn(false);

            given(userMapper.toDto(user, false))
                    .willReturn(userDto);

            // when
            UserDto result =
                    basicUserService.getUser(userId);

            // then
            assertThat(result.online())
                    .isFalse();

            then(jwtRegistry).should()
                    .hasActiveJwtInformationByUserId(userId);

            then(userMapper).should()
                    .toDto(user, false);
        }
    }

    @Nested
    @DisplayName("(수정) update")
    class Update {

        @Test
        @DisplayName("사용자가 존재하고 중복이 없으면 정보를 수정")
        void update_success() {
            // given
            UUID randomId = UUID.randomUUID();

            User user = sample();

            UserUpdateRequest request = new UserUpdateRequest(
                    "테스트 이름",
                    "테스트 이메일",
                    "테스트 패스워드"
            );

            String encodedPassword =
                    "암호화된 패스워드";

            UserDto updatedUserDto = new UserDto(
                    user.getId(),
                    request.newUsername(),
                    request.newEmail(),
                    null,
                    false,
                    Role.USER
            );

            given(userRepository.findById(randomId))
                    .willReturn(Optional.of(user));

            given(userRepository.existsByUsername(
                    request.newUsername()
            )).willReturn(false);

            given(userRepository.existsByEmail(
                    request.newEmail()
            )).willReturn(false);

            given(passwordEncoder.encode(
                    request.newPassword()
            )).willReturn(encodedPassword);

            given(jwtRegistry.hasActiveJwtInformationByUserId(randomId))
                    .willReturn(false);

            given(userMapper.toDto(user, false))
                    .willReturn(updatedUserDto);

            // when
            UserDto result =
                    basicUserService.updateUser(
                            randomId,
                            request,
                            null
                    );

            // then
            assertThat(user.getUsername())
                    .isEqualTo(request.newUsername());

            assertThat(user.getEmail())
                    .isEqualTo(request.newEmail());

            assertThat(user.getPassword())
                    .isEqualTo(encodedPassword);

            assertThat(result.username())
                    .isEqualTo(request.newUsername());

            assertThat(result.email())
                    .isEqualTo(request.newEmail());

            assertThat(result.role())
                    .isEqualTo(Role.USER);

            assertThat(result.online())
                    .isFalse();

            then(passwordEncoder).should()
                    .encode(request.newPassword());

            then(jwtRegistry).should()
                    .hasActiveJwtInformationByUserId(randomId);

            then(userMapper).should()
                    .toDto(user, false);
        }

        @Test
        @DisplayName("수정할 유저가 존재하지 않으면 예외 발생")
        void update_fail() {
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
            assertThatThrownBy(
                    () -> basicUserService.updateUser(
                            randomId,
                            request,
                            null
                    )
            ).isInstanceOf(
                    UserNotFoundException.class
            );

            then(userRepository).should()
                    .findById(randomId);

            then(userRepository).should(never())
                    .existsByUsername(any());

            then(passwordEncoder).should(never())
                    .encode(any());

            then(jwtRegistry).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("(삭제) delete")
    class Delete {

        @Test
        @DisplayName("사용자가 존재하면 삭제")
        void delete_success() {
            // given
            UUID randomId =
                    UUID.randomUUID();

            User user =
                    sample();

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
        void delete_fail() {
            // given
            UUID randomId =
                    UUID.randomUUID();

            given(userRepository.findById(randomId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                    () -> basicUserService.deleteUser(randomId)
            ).isInstanceOf(
                    UserNotFoundException.class
            );

            then(userRepository).should()
                    .findById(randomId);

            then(userRepository).should(never())
                    .deleteById(any());
        }
    }
}