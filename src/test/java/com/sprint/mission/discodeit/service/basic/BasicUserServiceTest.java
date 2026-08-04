package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
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
@DisplayName("BasicUserService 단위 테스트")
class BasicUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BinaryContentRepository binaryContentRepository;

    @Mock
    private BinaryContentService binaryContentService;

    @Mock
    private UserStatusRepository userStatusRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ReadStatusRepository readStatusRepository;

    @InjectMocks
    private BasicUserService userService;

    @Nested
    @DisplayName("사용자 생성")
    class Create {

        @Test
        @DisplayName("정상적인 요청이면 사용자를 생성한다")
        void createSuccess() {
            // given
            UserCreateRequest request = new UserCreateRequest(
                    "tester",
                    "tester@example.com",
                    "password1234",
                    null
            );

            given(userRepository.existsByUsername("tester"))
                    .willReturn(false);

            given(userRepository.existsByEmail("tester@example.com"))
                    .willReturn(false);

            given(userRepository.save(any(User.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            given(userStatusRepository.save(any(UserStatus.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            given(userStatusRepository.findByUserId(any()))
                    .willReturn(null);

            // when
            UserResponse response = userService.create(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo("tester");
            assertThat(response.getEmail()).isEqualTo("tester@example.com");
            assertThat(response.isOnline()).isFalse();

            then(userRepository)
                    .should()
                    .existsByUsername("tester");

            then(userRepository)
                    .should()
                    .existsByEmail("tester@example.com");

            then(userRepository)
                    .should()
                    .save(any(User.class));

            then(userStatusRepository)
                    .should()
                    .save(any(UserStatus.class));
        }

        @Test
        @DisplayName("사용자 이름이 중복되면 사용자 생성에 실패한다")
        void createFailsWhenUsernameDuplicated() {
            // given
            UserCreateRequest request = new UserCreateRequest(
                    "tester",
                    "tester@example.com",
                    "password1234",
                    null
            );

            given(userRepository.existsByUsername("tester"))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.create(request))
                    .isInstanceOf(UserAlreadyExistsException.class);

            then(userRepository)
                    .should()
                    .existsByUsername("tester");

            then(userRepository)
                    .should(never())
                    .existsByEmail(any());

            then(userRepository)
                    .should(never())
                    .save(any(User.class));

            then(userStatusRepository)
                    .should(never())
                    .save(any(UserStatus.class));
        }
    }

    @Nested
    @DisplayName("사용자 수정")
    class Update {

        @Test
        @DisplayName("존재하는 사용자의 정보를 정상적으로 수정한다")
        void updateSuccess() {
            // given
            User user = new User(
                    "oldUsername",
                    "old@example.com",
                    "oldPassword"
            );

            UUID userId = user.getId();

            UserUpdateRequest request = new UserUpdateRequest(
                    userId,
                    "newUsername",
                    "new@example.com",
                    "newPassword",
                    null,
                    null
            );

            given(userRepository.findById(userId))
                    .willReturn(Optional.of(user));

            given(userRepository.existsByUsername("newUsername"))
                    .willReturn(false);

            given(userRepository.existsByEmail("new@example.com"))
                    .willReturn(false);

            given(userRepository.save(any(User.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            given(userStatusRepository.findByUserId(userId))
                    .willReturn(null);

            // when
            UserResponse response = userService.update(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo("newUsername");
            assertThat(response.getEmail()).isEqualTo("new@example.com");

            assertThat(user.getUsername()).isEqualTo("newUsername");
            assertThat(user.getEmail()).isEqualTo("new@example.com");
            assertThat(user.getPassword()).isEqualTo("newPassword");

            then(userRepository)
                    .should()
                    .findById(userId);

            then(userRepository)
                    .should()
                    .existsByUsername("newUsername");

            then(userRepository)
                    .should()
                    .existsByEmail("new@example.com");

            then(userRepository)
                    .should()
                    .save(user);
        }

        @Test
        @DisplayName("존재하지 않는 사용자를 수정하면 예외가 발생한다")
        void updateFailsWhenUserDoesNotExist() {
            // given
            UUID userId = UUID.randomUUID();

            UserUpdateRequest request = new UserUpdateRequest(
                    userId,
                    "newUsername",
                    "new@example.com",
                    "newPassword",
                    null,
                    null
            );

            given(userRepository.findById(userId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.update(request))
                    .isInstanceOf(UserNotFoundException.class);

            then(userRepository)
                    .should()
                    .findById(userId);

            then(userRepository)
                    .should(never())
                    .existsByUsername(any());

            then(userRepository)
                    .should(never())
                    .existsByEmail(any());

            then(userRepository)
                    .should(never())
                    .save(any(User.class));
        }
    }

    @Nested
    @DisplayName("사용자 삭제")
    class Delete {

        @Test
        @DisplayName("존재하는 사용자를 정상적으로 삭제한다")
        void deleteSuccess() {
            // given
            User user = new User(
                    "tester",
                    "tester@example.com",
                    "password1234"
            );

            UUID userId = user.getId();

            given(userRepository.findById(userId))
                    .willReturn(Optional.of(user));

            given(messageRepository.findAllByAuthor_Id(userId))
                    .willReturn(List.of());

            // when
            userService.delete(userId);

            // then
            then(userRepository)
                    .should()
                    .findById(userId);

            then(messageRepository)
                    .should()
                    .findAllByAuthor_Id(userId);

            then(messageRepository)
                    .should()
                    .deleteByAuthor_Id(userId);

            then(readStatusRepository)
                    .should()
                    .deleteByUser_Id(userId);

            then(userStatusRepository)
                    .should()
                    .deleteByUserId(userId);

            then(userRepository)
                    .should()
                    .deleteById(userId);

            then(binaryContentService)
                    .should(never())
                    .delete(any(UUID.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자를 삭제하면 예외가 발생한다")
        void deleteFailsWhenUserDoesNotExist() {
            // given
            UUID userId = UUID.randomUUID();

            given(userRepository.findById(userId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.delete(userId))
                    .isInstanceOf(UserNotFoundException.class);

            then(userRepository)
                    .should()
                    .findById(userId);

            then(messageRepository)
                    .should(never())
                    .findAllByAuthor_Id(any(UUID.class));

            then(messageRepository)
                    .should(never())
                    .deleteByAuthor_Id(any(UUID.class));

            then(readStatusRepository)
                    .should(never())
                    .deleteByUser_Id(any(UUID.class));

            then(userStatusRepository)
                    .should(never())
                    .deleteByUserId(any(UUID.class));

            then(userRepository)
                    .should(never())
                    .deleteById(any(UUID.class));

            then(binaryContentService)
                    .should(never())
                    .delete(any(UUID.class));
        }
    }
}