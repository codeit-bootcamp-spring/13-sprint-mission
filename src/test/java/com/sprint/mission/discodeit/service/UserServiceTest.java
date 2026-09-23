package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.command.user.UserCreateCommand;
import com.sprint.mission.discodeit.dto.command.user.UserUpdateCommand;
import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserEmailDuplicatedException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserUsernameDuplicatedException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import com.sprint.mission.discodeit.service.basic.BinaryContentService;
import com.sprint.mission.discodeit.service.basic.ReadStatusService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String ENCODED_PASSWORD = "$2a$10$encoded-password-for-test";

    @InjectMocks
    BasicUserService userService;

    @Mock
    UserRepository userRepository;

    @Mock
    BinaryContentService binaryContentService;

    @Mock
    ReadStatusService readStatusService;

    @Mock
    MessageService messageService;

    @Mock
    UserMapper userMapper;

    @Mock
    PasswordEncoder passwordEncoder;

    @Nested
    @DisplayName("사용자 생성")
    class CreateUserTest {
        @Test
        @DisplayName("사용자 생성 성공")
        void create_returnsUserDto_whenValidCommand() {
            // given
            // 사용자 생성 요청 command를 실제 record 객체로 준비한다.
            // command는 단순 값 객체이므로 mock으로 만들 필요가 없고,
            // 실제 객체를 쓰는 편이 username/password/email 값 검증에 더 적합하다.
            UserCreateCommand command = createTestUserCommand();
            UUID userId = UUID.randomUUID();


            // mapper가 반환할 최종 UserDto도 실제 record 객체로 만든다.
            // 서비스 테스트는 mapper 내부 필드 매핑을 검증하지 않고,
            // mapper가 반환한 DTO가 서비스 반환값으로 이어지는지만 검증한다.
            UserDto expectedDto = createExpectedDto(userId, command, true);

            // 사용자 생성 전 이메일/사용자명 중복 검사를 모두 통과하는 상황을 명시한다.
            // boolean mock 기본값 false에 기대면 테스트 의도가 흐려지므로 성공 조건을 직접 stub 한다.
            given(userRepository.existsByEmail(command.email())).willReturn(false);
            given(userRepository.existsByUsername(command.username())).willReturn(false);
            given(passwordEncoder.encode(command.password())).willReturn(ENCODED_PASSWORD);

            // 이 테스트는 프로필 파일이 없는 기본 생성 케이스다.
            // BinaryContentService는 파일 저장소와 연결되는 협력 객체이므로 mock으로 두고 Optional.empty()를 반환하게 한다.
            given(binaryContentService.create(null)).willReturn(Optional.empty());

            // Repository는 mock이므로 실제 DB 저장이나 UUID 생성을 하지 않는다.
            // 서비스는 저장된 User를 mapper 변환에 사용하므로,
            // save(...)에 들어온 실제 User 엔티티에 id를 부여한 뒤 그대로 반환하게 한다.
            given(userRepository.save(any(User.class)))
                    .willAnswer(invocation -> {
                        User user = invocation.getArgument(0);
                        ReflectionTestUtils.setField(user, "id", userId);
                        return user;
                    });
            given(userMapper.toDto(any(User.class))).willReturn(expectedDto);

            // when
            // 실제 테스트 대상인 BasicUserService.create(...)를 실행한다.
            UserDto result = userService.create(command, null);

            // then
            // 서비스는 mapper가 만든 DTO를 그대로 반환해야 한다.
            assertThat(result).isEqualTo(expectedDto);

            // 저장 요청으로 만들어진 실제 User 엔티티를 캡처한다.
            // 반환 DTO만 검증하면 command 값이 User 생성자에 올바르게 들어갔는지 확인할 수 없다.
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getId()).isEqualTo(userId);
            assertThat(savedUser.getUsername()).isEqualTo(command.username());
            assertThat(savedUser.getPassword()).isEqualTo(ENCODED_PASSWORD);
            assertThat(savedUser.getPassword()).isNotEqualTo(command.password());
            assertThat(savedUser.getEmail()).isEqualTo(command.email());
            assertThat(savedUser.getProfile()).isNull();

            // 사용자 생성 성공 흐름의 핵심 협력 호출을 확인한다.
            verify(userRepository).existsByEmail(command.email());
            verify(userRepository).existsByUsername(command.username());
            verify(passwordEncoder).encode(command.password());
            verify(binaryContentService).create(null);
            verify(userMapper).toDto(savedUser);

            // create()는 읽음 상태 정리나 메시지 작성자 연결 해제와 무관하다.
            verifyNoInteractions(readStatusService, messageService);
            verifyNoMoreInteractions(userRepository, binaryContentService, userMapper, passwordEncoder);
        }

        @Test
        @DisplayName("사용자 생성 실패 - 이메일 중복")
        void create_throwsUserEmailDuplicatedException_whenEmailAlreadyExists() {
            // given
            // 테스트에서 사용할 사용자 생성 요청 객체를 준비한다.
            UserCreateCommand command = createTestUserCommand();

            // 이미 같은 이메일을 가진 사용자가 존재하는 상황을 만든다.
            // BasicUserService.create()는 사용자 생성 전에 existsByEmail()로 이메일 중복을 검사한다.
            given(userRepository.existsByEmail(command.email())).willReturn(true);

            // when & then
            // 중복된 이메일로 사용자를 생성하려고 하면 UserEmailDuplicatedException이 발생해야 한다.
            assertThatThrownBy(() -> userService.create(command, null))
                    .isInstanceOf(UserEmailDuplicatedException.class);

            // 이메일 중복 검증까지만 실행됐는지 확인한다.
            verify(userRepository).existsByEmail(command.email());
            verify(userRepository, never()).existsByUsername(anyString());

            // 이메일 중복 검증에서 예외가 발생했으므로 이후 생성 절차는 실행되면 안 된다.
            verify(binaryContentService, never()).create(any());
            verify(userRepository, never()).save(any(User.class));
            verifyNoInteractions(passwordEncoder);

        }

        @Test
        @DisplayName("사용자 생성 실패 - 사용자명 중복")
        void create_throwsUserUsernameDuplicatedException_whenUsernameAlreadyExists() {
            // given
            // 테스트에서 사용할 사용자 생성 요청 객체를 준비한다.
            UserCreateCommand command = createTestUserCommand();

            // BasicUserService.create()는 이메일 중복을 먼저 검사한 뒤 사용자명 중복을 검사한다.
            // 이 테스트는 "사용자명 중복" 케이스이므로 이메일은 중복되지 않은 상황으로 둔다.
            given(userRepository.existsByEmail(command.email())).willReturn(false);

            // 이미 같은 사용자명을 가진 사용자가 존재하는 상황을 만든다.
            given(userRepository.existsByUsername(command.username())).willReturn(true);

            // when & then
            // 중복된 사용자명으로 사용자를 생성하려고 하면 UserUsernameDuplicatedException 발생해야 한다.
            assertThatThrownBy(() -> userService.create(command, null))
                    .isInstanceOf(UserUsernameDuplicatedException.class);

            // 이메일 중복 검증 이후 사용자명 중복 검증까지 실행됐는지 확인한다.
            verify(userRepository).existsByEmail(command.email());
            verify(userRepository).existsByUsername(command.username());

            // 사용자명 중복 검증에서 예외가 발생했으므로 이후 생성 절차는 실행되면 안 된다.
            verify(binaryContentService, never()).create(any());
            verify(userRepository, never()).save(any(User.class));
            verifyNoInteractions(passwordEncoder);
        }

        @Test
        @DisplayName("사용자 생성 성공 - 프로필 이미지가 있으면 함께 저장")
        void create_returnsUserDtoWithProfile_whenProfileFileExists() {
            // given
            // 프로필 파일이 포함된 사용자 생성 요청을 준비한다.
            // command와 MultipartFile은 테스트 입력값이므로 실제 객체를 사용한다.
            UserCreateCommand command = createTestUserCommand();
            UUID userId = UUID.randomUUID();

            MockMultipartFile file = new MockMultipartFile(
                    "profile",
                    "profile.png",
                    "image/png",
                    "profile".getBytes()
            );

            // BinaryContentService가 파일 저장 후 반환할 프로필 엔티티를 준비한다.
            // 이 객체가 실제 User 생성자에 전달되어 저장되는지 ArgumentCaptor로 확인한다.
            BinaryContent profile = new BinaryContent("filename", "contentType", 1000L);

            UserDto expectedDto = createExpectedDto(userId, command, true);

            // 사용자 생성 가능 조건을 명시한다.
            given(userRepository.existsByEmail(command.email())).willReturn(false);
            given(userRepository.existsByUsername(command.username())).willReturn(false);
            given(passwordEncoder.encode(command.password())).willReturn(ENCODED_PASSWORD);

            // 프로필 파일 저장 결과로 BinaryContent가 반환되는 상황을 만든다.
            given(binaryContentService.create(file)).willReturn(Optional.of(profile));

            // mock repository는 실제 id 생성을 하지 않으므로 저장된 User에 id를 직접 넣어 반환한다.
            // 이렇게 하면 이후 mapper 변환에 사용되는 객체가 실제 저장 완료 상태와 더 가까워진다.
            given(userRepository.save(any(User.class)))
                    .willAnswer(invocation -> {
                        User user = invocation.getArgument(0);
                        ReflectionTestUtils.setField(user, "id", userId);
                        return user;
                    });
            given(userMapper.toDto(any(User.class))).willReturn(expectedDto);

            // when
            // 프로필 파일이 있는 사용자 생성 로직을 실행한다.
            UserDto result = userService.create(command, file);

            // then
            // 서비스는 mapper가 만든 DTO를 그대로 반환해야 한다.
            assertThat(result).isEqualTo(expectedDto);

            // 실제 저장된 User를 캡처해서 command 값과 profile이 반영됐는지 확인한다.
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getId()).isEqualTo(userId);
            assertThat(savedUser.getUsername()).isEqualTo(command.username());
            assertThat(savedUser.getPassword()).isEqualTo(ENCODED_PASSWORD);
            assertThat(savedUser.getPassword()).isNotEqualTo(command.password());
            assertThat(savedUser.getEmail()).isEqualTo(command.email());
            assertThat(savedUser.getProfile()).isEqualTo(profile);

            verify(userRepository).existsByEmail(command.email());
            verify(userRepository).existsByUsername(command.username());
            verify(passwordEncoder).encode(command.password());
            verify(binaryContentService).create(file);
            verify(userMapper).toDto(savedUser);

            // 사용자 생성은 읽음 상태 정리나 메시지 작성자 연결 해제와 무관하다.
            verifyNoInteractions(readStatusService, messageService);
            verifyNoMoreInteractions(userRepository, binaryContentService, userMapper, passwordEncoder);
        }

    }

    @Nested
    @DisplayName("사용자 조회")
    class FindUserTest {

        @Test
        @DisplayName("사용자 단건 조회 성공")
        void findById_returnsUserDto_whenUserExists() {
            // given
            // 조회 대상 userId와 repository가 반환할 실제 User 엔티티를 준비한다.
            // 이 테스트에서 User는 상태 변경 대상은 아니지만 생성 비용이 낮고 값 검증이 쉬우므로 실제 객체를 사용한다.
            UserCreateCommand command = createTestUserCommand();
            UUID userId = UUID.randomUUID();
            User user = new User(command, null);
            ReflectionTestUtils.setField(user, "id", userId);

            // mapper가 반환할 DTO는 실제 record 객체로 준비한다.
            // mapper는 mock이므로 실제 필드 매핑의 정확성은 UserMapper 테스트에서 별도로 검증한다.
            UserDto expectedDto = createExpectedDto(userId, command, false);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(userMapper.toDto(user)).willReturn(expectedDto);

            // when
            // 사용자 단건 조회 로직을 실행한다.
            UserDto result = userService.findById(userId);

            // then
            // 서비스는 mapper가 만든 DTO를 그대로 반환해야 한다.
            assertThat(result).isEqualTo(expectedDto);

            // 단건 조회의 핵심 협력은 repository 조회와 mapper 변환이다.
            verify(userRepository).findById(userId);
            verify(userMapper).toDto(user);

            // findById()는 파일 저장, 사용자 상태 생성/삭제, 읽음 상태 정리, 메시지 작성자 연결 해제를 수행하지 않는다.
            verifyNoInteractions(binaryContentService, readStatusService, messageService);
            verifyNoMoreInteractions(userRepository, userMapper);
        }

        @Test
        @DisplayName("사용자 단건 조회 실패 - 존재하지 않는 사용자")
        void findById_throwsUserNotFoundException_whenUserDoesNotExist() {
            // given
            // 조회할 userId는 있지만 repository가 해당 사용자를 찾지 못하는 상황을 만든다.
            UUID userId = UUID.randomUUID();

            // BasicUserService.findById(...)는 repository.findById(...) 결과가 비어 있으면
            // 후속 mapper 변환 없이 UserNotFoundException을 던져야 한다.
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            // 존재하지 않는 사용자를 조회하면 UserNotFoundException이 발생해야 한다.
            assertThatThrownBy(() -> userService.findById(userId))
                    .isInstanceOf(UserNotFoundException.class);

            // 실패 흐름에서도 조회 시도 자체는 반드시 있어야 한다.
            verify(userRepository).findById(userId);
            verifyNoMoreInteractions(userRepository);

            // 사용자를 찾지 못했으므로 DTO 변환이나 다른 부가 작업은 실행되면 안 된다.
            verifyNoInteractions(userMapper, binaryContentService, readStatusService, messageService);
        }

        @Test
        @DisplayName("사용자 목록 조회 성공 - 사용자 목록 반환")
        void findAll_returnsUserDtoList_whenUsersExist() {
            // given
            // repository가 반환할 실제 User 엔티티 목록을 준비한다.
            // 목록 조회에서는 User 상태 변경은 없지만, 단순 값 객체보다 실제 엔티티를 쓰면 mapper 전달 인자를 명확히 검증할 수 있다.
            List<UserCreateCommand> commands = List.of(createTestUserCommand(), createTestUserCommand());
            List<User> users = commands.stream()
                    .map(command -> new User(command, null))
                    .toList();

            // mapper가 각 User별로 반환할 실제 UserDto 목록을 준비한다.
            List<UserDto> userDtos = commands.stream()
                    .map(UserServiceTest.this::createExpectedDto)
                    .toList();

            given(userRepository.findAll()).willReturn(users);
            given(userMapper.toDto(users.get(0))).willReturn(userDtos.get(0));
            given(userMapper.toDto(users.get(1))).willReturn(userDtos.get(1));

            // when
            // 사용자 목록 조회 서비스 로직을 실행한다.
            List<UserDto> results = userService.findAll();

            // then
            // 서비스는 repository 조회 순서를 유지한 채 mapper 결과를 리스트로 반환해야 한다.
            assertThat(results).containsExactlyElementsOf(userDtos);

            // 조회된 각 User가 mapper에 정확히 한 번씩 전달됐는지 확인한다.
            verify(userRepository).findAll();
            verify(userMapper).toDto(users.get(0));
            verify(userMapper).toDto(users.get(1));

            // findAll()은 조회 전용이므로 파일/상태/메시지 정리 협력 객체를 사용하지 않는다.
            verifyNoInteractions(binaryContentService, readStatusService, messageService);
            verifyNoMoreInteractions(userRepository, userMapper);
        }

        @Test
        @DisplayName("사용자 목록 조회 성공 - 사용자가 없으면 빈 목록 반환")
        void findAll_returnsEmptyList_whenUsersDoNotExist() {
            // given
            // repository.findAll() 결과가 빈 리스트인 상황을 만든다.
            // 이 케이스는 예외가 아니라 정상적으로 빈 DTO 목록을 반환해야 하는 흐름이다.
            given(userRepository.findAll()).willReturn(List.of());

            // when
            // 사용자 목록 조회 서비스 로직을 실행한다.
            List<UserDto> results = userService.findAll();

            // then
            // 조회된 사용자가 없으면 null이 아니라 빈 리스트를 반환해야 한다.
            assertThat(results).isEmpty();

            // 사용자 목록 조회는 repository.findAll()까지만 실행된다.
            verify(userRepository).findAll();
            verifyNoMoreInteractions(userRepository);

            // 변환할 User가 없으므로 mapper는 호출되지 않아야 한다.
            // 조회 전용 흐름이므로 다른 부가 협력 객체도 사용하지 않는다.
            verifyNoInteractions(userMapper, binaryContentService, readStatusService, messageService);
        }
    }

    @Nested
    @DisplayName("사용자 수정")
    class UpdateUserTest {
        @Test
        @DisplayName("사용자 수정 성공")
        void update_returnsUpdatedUserDto_whenValidCommand() {
            // given
            // update()는 먼저 userId로 기존 사용자를 조회한다.
            // 따라서 repository가 반환할 기존 User 엔티티를 준비한다.
            UserCreateCommand command = createTestUserCommand();
            UUID userId = UUID.randomUUID();
            User user = new User(command, null);

            // 기존 사용자와 다른 값으로 수정 요청을 만들어
            // 중복 검사와 엔티티 수정 로직이 모두 실행되도록 한다.
            UserUpdateCommand updateCommand = createUpdateCommand();

            // 서비스는 수정된 User를 직접 DTO로 만들지 않고 mapper에 위임한다.
            // 따라서 mapper가 반환할 DTO를 미리 정해 최종 반환값을 검증한다.
            UserDto expectedDto = createExpectedDto(userId, updateCommand, false);

            // userId로 조회하면 준비한 User 엔티티가 존재하는 상황을 만든다.
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // 변경할 이메일이 기존 사용자와 다르므로 중복 검사가 실행된다.
            // 성공 케이스이므로 같은 이메일은 존재하지 않는다고 설정한다.
            given(userRepository.existsByEmail(updateCommand.email())).willReturn(false);

            // 변경할 사용자명도 기존 사용자와 다르므로 중복 검사가 실행된다.
            // 성공 케이스이므로 같은 사용자명은 존재하지 않는다고 설정한다.
            given(userRepository.existsByUsername(updateCommand.username())).willReturn(false);

            // 이번 테스트는 프로필 이미지 변경이 없는 사용자 정보 수정만 검증한다.
            // 따라서 파일 생성 결과는 Optional.empty()로 설정한다.
            given(binaryContentService.create(null)).willReturn(Optional.empty());
            given(passwordEncoder.encode(updateCommand.password())).willReturn(ENCODED_PASSWORD);

            // update()는 수정된 User를 저장한 뒤 저장 결과를 mapper에 넘긴다.
            // mock repository는 기본적으로 null을 반환하므로 명시적으로 user를 반환하게 한다.
            given(userRepository.save(user)).willReturn(user);

            // 최종 반환값 검증을 위해 mapper가 expectedDto를 반환하도록 설정한다.
            given(userMapper.toDto(user)).willReturn(expectedDto);

            // when
            UserDto updateUserDto = userService.update(userId, updateCommand, null);

            // then
            assertThat(updateUserDto).isEqualTo(expectedDto);

            // 반환 DTO뿐 아니라 실제 User 엔티티 상태도 수정됐는지 확인한다.
            assertThat(user.getUsername()).isEqualTo(updateCommand.username());
            assertThat(user.getPassword()).isEqualTo(ENCODED_PASSWORD);
            assertThat(user.getPassword()).isNotEqualTo(updateCommand.password());
            assertThat(user.getEmail()).isEqualTo(updateCommand.email());

            // update()가 기대한 협력 객체들을 호출했는지 확인한다.
            verify(userRepository).findById(userId);
            verify(userRepository).existsByEmail(updateCommand.email());
            verify(userRepository).existsByUsername(updateCommand.username());
            verify(passwordEncoder).encode(updateCommand.password());
            verify(binaryContentService).create(null);
            verify(userRepository).save(user);
            verify(userMapper).toDto(user);

        }

        @Test
        @DisplayName("사용자 수정 실패 - 존재하지 않는 사용자")
        void update_throwsUserNotFoundException_whenUserDoesNotExist() {
            // given
            // update()는 먼저 userId로 수정할 사용자를 조회한다.
            // 존재하지 않는 사용자 수정 요청을 검증하기 위해 임의의 userId를 준비한다.
            UUID userId = UUID.randomUUID();

            // 사용자가 존재하지 않아도 update() 호출에는 수정 요청 객체가 필요하다.
            // 이 테스트에서는 조회 실패가 목적이므로 command의 값 자체는 중요하지 않다.
            UserUpdateCommand updateCommand = createUpdateCommand();

            // userId로 조회했을 때 사용자가 없는 상황을 만든다.
            // Optional.empty()가 반환되면 서비스는 UserNotFoundException을 던져야 한다.
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            // 존재하지 않는 사용자를 수정하려고 하면 UserNotFoundException이 발생해야 한다.
            assertThatThrownBy(() -> userService.update(userId, updateCommand, null))
                    .isInstanceOf(UserNotFoundException.class);

            // 조회 단계에서 예외가 발생했으므로 이후 수정 절차는 실행되면 안 된다.
            verify(userRepository).findById(userId);
            verify(userRepository, never()).existsByEmail(anyString());
            verify(userRepository, never()).existsByUsername(anyString());
            verify(binaryContentService, never()).create(any());
            verify(userRepository, never()).save(any(User.class));
            verify(userMapper, never()).toDto(any(User.class));
        }

        @Test
        @DisplayName("사용자 수정 실패 - 이메일 중복")
        void update_throwsUserEmailDuplicatedException_whenEmailAlreadyExists() {
            // given
            // update()는 먼저 userId로 수정 대상 사용자를 조회한다.
            // 이 테스트는 조회 성공 이후 이메일 중복 검증에서 실패하는 상황을 확인한다.
            UserCreateCommand command = createTestUserCommand();
            UUID userId = UUID.randomUUID();
            User user = new User(command, null);

            // 기존 사용자와 다른 이메일로 수정 요청을 준비한다.
            // 그래야 서비스가 이메일 중복 검사를 실행한다.
            UserUpdateCommand updateCommand = createUpdateCommand();

            // userId로 조회하면 수정 대상 사용자가 존재하는 상황을 만든다.
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // 변경하려는 이메일이 이미 다른 사용자에게 사용 중인 상황을 만든다.
            // 이 값이 true이면 서비스는 UserEmailDuplicatedException을 던져야 한다.
            given(userRepository.existsByEmail(updateCommand.email())).willReturn(true);

            // when & then
            // 이메일이 중복된 사용자 수정 요청은 실패해야 한다.
            assertThatThrownBy(() -> userService.update(userId, updateCommand, null))
                    .isInstanceOf(UserEmailDuplicatedException.class);

            // 수정 대상 조회와 이메일 중복 검사까지 실행됐는지 확인한다.
            verify(userRepository).findById(userId);
            verify(userRepository).existsByEmail(updateCommand.email());

            // 이메일 중복 단계에서 예외가 발생했으므로 이후 로직은 실행되면 안 된다.
            verify(userRepository, never()).existsByUsername(anyString());
            verify(binaryContentService, never()).create(any());
            verify(userRepository, never()).save(any(User.class));
            verify(userMapper, never()).toDto(any(User.class));
        }

        @Test
        @DisplayName("사용자 수정 실패 - 사용자명 중복")
        void update_throwsUserUsernameDuplicatedException_whenUsernameAlreadyExists() {
            // given
            // update()는 먼저 userId로 수정 대상 사용자를 조회한다.
            // 이 테스트는 사용자는 존재하지만, 변경하려는 사용자명이 이미 사용 중인 경우를 검증한다.
            UserCreateCommand command = createTestUserCommand();
            UUID userId = UUID.randomUUID();
            User user = new User(command, null);

            // 이메일은 기존 이메일 그대로 사용한다.
            // 그래야 이메일 중복 검사는 건너뛰고, 사용자명 중복 검사만 검증할 수 있다.
            UserUpdateCommand updateCommand = new UserUpdateCommand(
                    "duplicatedUsername",
                    "changePassword",
                    command.email()
            );

            // userId로 조회하면 수정 대상 사용자가 존재하는 상황을 만든다.
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // 변경하려는 사용자명이 이미 다른 사용자에게 사용 중인 상황을 만든다.
            // 이 값이 true이면 서비스는 UserUsernameDuplicatedException을 던져야 한다.
            given(userRepository.existsByUsername(updateCommand.username())).willReturn(true);

            // when & then
            // 사용자명이 중복된 사용자 수정 요청은 실패해야 한다.
            assertThatThrownBy(() -> userService.update(userId, updateCommand, null))
                    .isInstanceOf(UserUsernameDuplicatedException.class);

            // 수정 대상 조회와 사용자명 중복 검사까지 실행됐는지 확인한다.
            verify(userRepository).findById(userId);
            verify(userRepository).existsByUsername(updateCommand.username());

            // 이메일은 기존 값과 같으므로 이메일 중복 검사는 실행되지 않아야 한다.
            verify(userRepository, never()).existsByEmail(anyString());

            // 사용자명 중복 단계에서 예외가 발생했으므로 이후 수정 절차는 실행되면 안 된다.
            verify(binaryContentService, never()).create(any());
            verify(userRepository, never()).save(any(User.class));
            verify(userMapper, never()).toDto(any(User.class));
        }

        @Test
        @DisplayName("사용자 수정 성공 - 이메일과 사용자명이 같으면 중복 검사를 건너뜀")
        void update_skipsDuplicateChecks_whenUsernameAndEmailAreUnchanged() {
            // given
            // update()는 먼저 userId로 수정 대상 사용자를 조회한다.
            // 이 테스트는 사용자명과 이메일이 기존 값과 같을 때 중복 검사를 건너뛰는지 확인한다.
            UserCreateCommand command = createTestUserCommand();
            UUID userId = UUID.randomUUID();
            User user = new User(command, null);

            // 사용자명과 이메일은 기존 값 그대로 사용하고, 비밀번호만 변경한다.
            // 서비스는 사용자명과 이메일이 모두 기존 값과 같으면 중복 검사 없이 수정을 진행한다.
            UserUpdateCommand updateCommand = new UserUpdateCommand(
                    command.username(),
                    "changePassword",
                    command.email()
            );

            // 서비스는 수정된 User를 mapper에 넘겨 DTO로 변환한다.
            // mapper가 반환할 최종 DTO를 미리 준비한다.
            UserDto expectedDto = createExpectedDto(userId, updateCommand, false);

            // userId로 조회하면 수정 대상 사용자가 존재하는 상황을 만든다.
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // 이 테스트는 프로필 이미지 변경 없이 사용자 정보만 수정하는 케이스다.
            given(binaryContentService.create(null)).willReturn(Optional.empty());
            given(passwordEncoder.encode(updateCommand.password())).willReturn(ENCODED_PASSWORD);

            // mock repository는 기본적으로 save() 호출 시 null을 반환한다.
            // 서비스는 save() 결과를 mapper에 넘기므로 저장된 user를 반환하게 설정한다.
            given(userRepository.save(user)).willReturn(user);

            // 최종 반환값 검증을 위해 mapper가 expectedDto를 반환하도록 설정한다.
            given(userMapper.toDto(user)).willReturn(expectedDto);

            // when
            UserDto resultDto = userService.update(userId, updateCommand, null);

            // then
            // 서비스 반환값이 mapper가 반환한 DTO와 같은지 확인한다.
            assertThat(resultDto).isEqualTo(expectedDto);

            // 비밀번호는 인코더가 반환한 해시로 수정되어야 한다.
            assertThat(user.getPassword()).isEqualTo(ENCODED_PASSWORD);
            assertThat(user.getPassword()).isNotEqualTo(updateCommand.password());

            // 수정 대상 사용자를 조회했는지 확인한다.
            verify(userRepository).findById(userId);

            // 사용자명과 이메일이 기존 값과 같으므로 중복 검사는 실행되지 않아야 한다.
            verify(userRepository, never()).existsByEmail(anyString());
            verify(userRepository, never()).existsByUsername(anyString());
            verify(passwordEncoder).encode(updateCommand.password());

            // 프로필 이미지 변경은 없지만, 서비스는 파일 생성 시도를 하고 Optional.empty()를 받는다.
            verify(binaryContentService).create(null);

            // 중복 검사 없이 사용자 수정과 저장은 정상적으로 진행되어야 한다.
            verify(userRepository).save(user);

            // 기존 프로필 이미지가 없으므로 파일 삭제는 실행되지 않아야 한다.
            verify(binaryContentService, never()).delete(any(BinaryContent.class));

            // 저장된 User가 DTO로 변환되어야 한다.
            verify(userMapper).toDto(user);

        }

        @Test
        @DisplayName("사용자 수정 성공 - 비밀번호가 없으면 기존 해시를 유지")
        void update_keepsExistingPassword_whenPasswordIsNull() {
            // given
            UserCreateCommand storedCommand = new UserCreateCommand(
                    "testUsername",
                    ENCODED_PASSWORD,
                    "test@gmail.com"
            );
            User user = new User(storedCommand, null);
            UUID userId = UUID.randomUUID();
            UserUpdateCommand updateCommand = new UserUpdateCommand(
                    storedCommand.username(),
                    null,
                    storedCommand.email()
            );
            UserDto expectedDto = createExpectedDto(userId, updateCommand, false);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(binaryContentService.create(null)).willReturn(Optional.empty());
            given(userRepository.save(user)).willReturn(user);
            given(userMapper.toDto(user)).willReturn(expectedDto);

            // when
            UserDto result = userService.update(userId, updateCommand, null);

            // then
            assertThat(result).isEqualTo(expectedDto);
            assertThat(user.getPassword()).isEqualTo(ENCODED_PASSWORD);
            verifyNoInteractions(passwordEncoder);
            verify(userRepository).findById(userId);
            verify(binaryContentService).create(null);
            verify(userRepository).save(user);
            verify(userMapper).toDto(user);
        }

        @Test
        @DisplayName("사용자 수정 성공 - 새 프로필 이미지가 있으면 기존 이미지를 교체")
        void update_replacesProfileImage_whenNewProfileFileExists() {
            // given
            // update()는 먼저 userId로 수정 대상 사용자를 조회한다.
            // 이 테스트는 기존 프로필 이미지가 있는 사용자가 새 프로필 이미지로 수정될 때,
            // 새 이미지로 교체되고 기존 이미지가 삭제되는지 확인한다.
            UserCreateCommand command = createTestUserCommand();
            UUID userId = UUID.randomUUID();

            // 기존 프로필과 새 프로필의 id가 달라야 기존 프로필 삭제 분기가 실행된다.
            UUID oldProfileId = UUID.randomUUID();
            BinaryContent oldProfile = new BinaryContent("file", "contentType", 1000L);
            ReflectionTestUtils.setField(oldProfile, "id", oldProfileId);

            User user = new User(command, oldProfile);

            MockMultipartFile newFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
            UUID newProfileId = UUID.randomUUID();
            BinaryContent newProfileContent = new BinaryContent(newFile.getOriginalFilename(), newFile.getContentType(), newFile.getSize());
            ReflectionTestUtils.setField(newProfileContent, "id", newProfileId);

            UserUpdateCommand updateCommand = createUpdateCommand();

            BinaryContentDto newProfile = new BinaryContentDto(newProfileId, newFile.getOriginalFilename(), newFile.getSize(), newFile.getContentType());
            UserDto expectedDto = createExpectedDto(userId, updateCommand, false, newProfile);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(userRepository.existsByEmail(updateCommand.email())).willReturn(false);
            given(userRepository.existsByUsername(updateCommand.username())).willReturn(false);
            given(passwordEncoder.encode(updateCommand.password())).willReturn(ENCODED_PASSWORD);
            given(binaryContentService.create(newFile)).willReturn(Optional.of(newProfileContent));
            given(userRepository.save(user)).willReturn(user);
            given(userMapper.toDto(user)).willReturn(expectedDto);

            // when
            UserDto result = userService.update(userId, updateCommand, newFile);

            // then
            assertThat(result).isEqualTo(expectedDto);

            // 사용자 정보와 프로필이 새 요청 값으로 변경됐는지 확인한다.
            assertThat(user.getUsername()).isEqualTo(updateCommand.username());
            assertThat(user.getEmail()).isEqualTo(updateCommand.email());
            assertThat(user.getProfile()).isEqualTo(newProfileContent);

            verify(userRepository).findById(userId);
            verify(userRepository).existsByEmail(updateCommand.email());
            verify(userRepository).existsByUsername(updateCommand.username());
            verify(passwordEncoder).encode(updateCommand.password());
            verify(binaryContentService).create(newFile);
            verify(userRepository).save(user);

            // 새 프로필로 교체됐으므로 기존 프로필 파일은 삭제되어야 한다.
            verify(binaryContentService).delete(oldProfile);

            verify(userMapper).toDto(user);

        }


        @Test
        @DisplayName("사용자 수정 성공 - 새 프로필 이미지가 없으면 기존 이미지를 유지")
        void update_keepsExistingProfileImage_whenNewProfileFileDoesNotExist() {
            // given
            // update()는 먼저 userId로 수정 대상 사용자를 조회한다.
            // 이 테스트는 기존 프로필 이미지가 있는 사용자가 새 파일 없이 수정될 때,
            // 기존 프로필 이미지가 유지되고 파일 삭제가 실행되지 않는지 확인한다.
            UserCreateCommand command = createTestUserCommand();
            UUID userId = UUID.randomUUID();

            // 기존 프로필 이미지를 가진 사용자를 준비한다.
            // binaryContentService.create(null)가 Optional.empty()를 반환하면 서비스는 이 기존 프로필을 그대로 사용한다.
            UUID profileId = UUID.randomUUID();
            BinaryContent profile = new BinaryContent("file", "contentType", 1000L);
            ReflectionTestUtils.setField(profile, "id", profileId);

            User user = new User(command, profile);

            UserUpdateCommand updateCommand = createUpdateCommand();

            BinaryContentDto profileDto = new BinaryContentDto(profileId, profile.getOriginalFileName(), profile.getSize(), profile.getContentType());
            UserDto expectedDto = createExpectedDto(userId, updateCommand, false, profileDto);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(userRepository.existsByEmail(updateCommand.email())).willReturn(false);
            given(userRepository.existsByUsername(updateCommand.username())).willReturn(false);
            given(passwordEncoder.encode(updateCommand.password())).willReturn(ENCODED_PASSWORD);

            // 새 파일이 없으므로 파일 생성 결과는 Optional.empty()다.
            // 서비스는 새 이미지 대신 기존 profile을 유지해야 한다.
            given(binaryContentService.create(null)).willReturn(Optional.empty());

            given(userRepository.save(user)).willReturn(user);
            given(userMapper.toDto(user)).willReturn(expectedDto);

            // when
            UserDto result = userService.update(userId, updateCommand, null);

            // then
            assertThat(result).isEqualTo(expectedDto);

            // 새 프로필이 없으므로 기존 프로필이 그대로 유지되어야 한다.
            assertThat(user.getProfile()).isEqualTo(profile);
            assertThat(user.getProfile()).isNotNull();

            // 사용자 기본 정보는 updateCommand 값으로 수정되어야 한다.
            assertThat(user.getUsername()).isEqualTo(updateCommand.username());
            assertThat(user.getEmail()).isEqualTo(updateCommand.email());

            verify(userRepository).findById(userId);
            verify(userRepository).existsByEmail(updateCommand.email());
            verify(userRepository).existsByUsername(updateCommand.username());
            verify(passwordEncoder).encode(updateCommand.password());
            verify(binaryContentService).create(null);
            verify(userRepository).save(user);

            // 기존 프로필을 그대로 유지하므로 파일 삭제는 실행되면 안 된다.
            verify(binaryContentService, never()).delete(any(BinaryContent.class));

            verify(userMapper).toDto(user);

        }

        @Test
        @DisplayName("사용자 수정 성공 - 이메일만 변경되면 이메일 중복만 검사")
        void update_checksOnlyEmailDuplicate_whenOnlyEmailChanged() {
            // given
            // update()는 먼저 userId로 수정 대상 사용자를 조회한다.
            // 이 테스트는 사용자명은 그대로이고 이메일만 변경될 때,
            // 이메일 중복 검사만 실행되고 사용자명 중복 검사는 건너뛰는지 확인한다.
            UserCreateCommand command = createTestUserCommand();
            UUID userId = UUID.randomUUID();
            User user = new User(command, null);

            // 사용자명은 기존 값 그대로 사용하고 이메일은 새 값으로 변경한다.
            // 서비스는 변경된 이메일에 대해서만 중복 검사를 수행해야 한다.
            UserUpdateCommand updateCommand = new UserUpdateCommand(
                    command.username(),
                    "changePassword",
                    "changeEmail@gmail.com"
            );
            UserDto expectedDto = createExpectedDto(userId, updateCommand, false);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // 이메일만 변경되므로 이메일 중복 검사 결과만 준비한다.
            given(userRepository.existsByEmail(updateCommand.email())).willReturn(false);
            given(passwordEncoder.encode(updateCommand.password())).willReturn(ENCODED_PASSWORD);

            // 이 테스트는 프로필 이미지 변경 없이 사용자 정보만 수정하는 케이스다.
            given(binaryContentService.create(null)).willReturn(Optional.empty());
            given(userRepository.save(user)).willReturn(user);
            given(userMapper.toDto(user)).willReturn(expectedDto);

            // when
            UserDto result = userService.update(userId, updateCommand, null);

            // then
            assertThat(result).isEqualTo(expectedDto);

            // 이메일은 변경 요청 값으로 수정되어야 한다.
            assertThat(user.getEmail()).isEqualTo(updateCommand.email());

            // 사용자명은 기존 값이 유지되어야 한다.
            assertThat(user.getUsername()).isEqualTo(command.username());

            verify(userRepository).findById(userId);

            // 이메일만 변경됐으므로 이메일 중복 검사만 실행되어야 한다.
            verify(userRepository).existsByEmail(updateCommand.email());
            verify(userRepository, never()).existsByUsername(anyString());
            verify(passwordEncoder).encode(updateCommand.password());

            verify(binaryContentService).create(null);
            verify(userRepository).save(user);

            // 기존 프로필 이미지가 없으므로 파일 삭제는 실행되지 않아야 한다.
            verify(binaryContentService, never()).delete(any(BinaryContent.class));

            verify(userMapper).toDto(user);

        }

        @Test
        @DisplayName("사용자 수정 성공 - 사용자명만 변경되면 사용자명 중복만 검사")
        void update_checksOnlyUsernameDuplicate_whenOnlyUsernameChanged() {
            // given
            // update()는 먼저 userId로 수정 대상 사용자를 조회한다.
            // 이 테스트는 이메일은 그대로이고 사용자명만 변경될 때,
            // 사용자명 중복 검사만 실행되고 이메일 중복 검사는 건너뛰는지 확인한다.
            UserCreateCommand command = createTestUserCommand();
            UUID userId = UUID.randomUUID();
            User user = new User(command, null);

            // 이메일은 기존 값 그대로 사용하고 사용자명은 새 값으로 변경한다.
            // 서비스는 변경된 사용자명에 대해서만 중복 검사를 수행해야 한다.
            UserUpdateCommand updateCommand = new UserUpdateCommand(
                    "changeUsername",
                    "changePassword",
                    command.email()
            );
            UserDto expectedDto = createExpectedDto(userId, updateCommand, false);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // 사용자명만 변경되므로 사용자명 중복 검사 결과만 준비한다.
            given(userRepository.existsByUsername(updateCommand.username())).willReturn(false);
            given(passwordEncoder.encode(updateCommand.password())).willReturn(ENCODED_PASSWORD);

            // 이 테스트는 프로필 이미지 변경 없이 사용자 정보만 수정하는 케이스다.
            given(binaryContentService.create(null)).willReturn(Optional.empty());

            given(userRepository.save(user)).willReturn(user);
            given(userMapper.toDto(user)).willReturn(expectedDto);

            // when
            UserDto result = userService.update(userId, updateCommand, null);

            // then
            assertThat(result).isEqualTo(expectedDto);

            // 사용자명은 변경 요청 값으로 수정되어야 한다.
            assertThat(user.getUsername()).isEqualTo(updateCommand.username());

            // 이메일은 기존 값이 유지되어야 한다.
            assertThat(user.getEmail()).isEqualTo(command.email());

            verify(userRepository).findById(userId);

            // 사용자명만 변경됐으므로 사용자명 중복 검사만 실행되어야 한다.
            verify(userRepository).existsByUsername(updateCommand.username());
            verify(userRepository, never()).existsByEmail(anyString());
            verify(passwordEncoder).encode(updateCommand.password());

            verify(binaryContentService).create(null);
            verify(userRepository).save(user);

            // 기존 프로필 이미지가 없으므로 파일 삭제는 실행되지 않아야 한다.
            verify(binaryContentService, never()).delete(any(BinaryContent.class));

            verify(userMapper).toDto(user);

        }

    }

    @Nested
    @DisplayName("사용자 삭제")
    class DeleteUserTest {

        @Test
        @DisplayName("사용자 삭제 성공")
        void delete_deletesUser_whenUserExists() {
            // given
            // 삭제 대상 userId와 실제 User 엔티티를 준비한다.
            // delete()는 user.getStatusId(), user.getId(), user.isProfileImageExist()를 사용하므로
            // User를 mock으로 만들기보다 실제 엔티티에 id를 넣어 사용하는 편이 안전하다.
            UserCreateCommand command = createTestUserCommand();
            UUID userId = UUID.randomUUID();
            User user = new User(command, null);
            ReflectionTestUtils.setField(user, "id", userId);

            // userId로 조회하면 삭제 대상 사용자가 존재하는 상황을 만든다.
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            // 사용자 삭제 로직을 실행한다.
            userService.delete(userId);

            // then
            // 삭제 성공 흐름은 순서가 중요하다.
            // 1. 삭제 대상 사용자 조회
            // 2. 읽음 상태 삭제
            // 3. 메시지 작성자 연결 해제
            // 4. 사용자 엔티티 삭제
            // 프로필 이미지가 없으면 파일 삭제는 실행되지 않는다.
            InOrder inOrder = inOrder(userRepository, readStatusService, messageService);
            inOrder.verify(userRepository).findById(userId);
            inOrder.verify(readStatusService).deleteByUserId(userId);
            inOrder.verify(messageService).detachByAuthorId(userId);
            inOrder.verify(userRepository).deleteById(userId);

            // 프로필 이미지가 없는 사용자이므로 파일 삭제는 실행되지 않아야 한다.
            verify(binaryContentService, never()).delete(any(BinaryContent.class));

            // delete()는 DTO를 반환하지 않으므로 mapper를 사용하지 않는다.
            verifyNoInteractions(userMapper);
            verifyNoMoreInteractions(userRepository, readStatusService, messageService, binaryContentService);
        }

        @Test
        @DisplayName("사용자 삭제 실패 - 존재하지 않는 사용자")
        void delete_throwsUserNotFoundException_whenUserDoesNotExist() {
            // given
            // 삭제할 userId는 있지만 repository가 해당 사용자를 찾지 못하는 상황을 만든다.
            UUID userId = UUID.randomUUID();

            // BasicUserService.delete(...)는 가장 먼저 삭제 대상 User를 조회한다.
            // Optional.empty()이면 연결 데이터 정리나 deleteById(...)로 넘어가지 않고 예외가 발생해야 한다.
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            // 삭제 대상 사용자가 없으면 UserNotFoundException이 발생해야 한다.
            assertThatThrownBy(() -> userService.delete(userId))
                    .isInstanceOf(UserNotFoundException.class);

            // 삭제 흐름은 사용자 조회에서 시작하므로 findById(userId) 호출은 반드시 있어야 한다.
            verify(userRepository).findById(userId);
            verifyNoMoreInteractions(userRepository);

            // 조회 단계에서 예외가 발생했으므로 연결 데이터 정리, 파일 삭제, DTO 변환은 실행되면 안 된다.
            verifyNoInteractions( readStatusService, messageService, binaryContentService, userMapper);
        }

        @Test
        @DisplayName("사용자 삭제 성공 - 프로필 이미지가 있으면 파일도 삭제")
        void delete_deletesProfileImage_whenUserHasProfile() {
            // given
            // 프로필 이미지가 있는 사용자를 삭제하는 상황을 준비한다.
            // User.isProfileImageExist()와 User.getProfile()의 실제 동작이 필요하므로 실제 User/파일 엔티티를 사용한다.
            UserCreateCommand command = createTestUserCommand();
            UUID userId = UUID.randomUUID();

            BinaryContent profile = new BinaryContent("filename", "contentType", 1000L);
            User user = new User(command, profile);
            ReflectionTestUtils.setField(user, "id", userId);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            // when
            // 사용자 삭제 로직을 실행한다.
            userService.delete(userId);

            // then
            // 프로필이 있는 경우에도 먼저 사용자와 연결된 도메인 데이터를 정리한 뒤 사용자 엔티티를 삭제한다.
            // 파일 삭제는 DB 사용자 삭제 요청 이후에 수행되는 후처리다.
            InOrder inOrder = inOrder(userRepository, readStatusService, messageService, binaryContentService);
            inOrder.verify(userRepository).findById(userId);
            inOrder.verify(readStatusService).deleteByUserId(userId);
            inOrder.verify(messageService).detachByAuthorId(userId);
            inOrder.verify(userRepository).deleteById(userId);
            inOrder.verify(binaryContentService).delete(profile);

            // delete()는 DTO를 반환하지 않으므로 mapper를 사용하지 않는다.
            verifyNoInteractions(userMapper);
            verifyNoMoreInteractions(userRepository, readStatusService, messageService, binaryContentService);
        }
    }


    private UserCreateCommand createTestUserCommand() {
        String s = retrieveShortUUID();
        return new UserCreateCommand("test" + s, "test", "test" + s + "@gamil.com");
    }

    private String retrieveShortUUID() {
        return UUID.randomUUID().toString().substring(0, 4);
    }

    private UserDto createExpectedDto(UUID userId, UserCreateCommand command, boolean isOnline) {
        return new UserDto(
                userId,
                command.username(),
                command.email(),
                null,
                isOnline,
                Role.USER,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }


    private UserDto createExpectedDto(UUID userId, UserUpdateCommand updateCommand, boolean isOnline) {
        return createExpectedDto(userId, updateCommand, isOnline, null);
    }

    private UserDto createExpectedDto(UUID userId, UserUpdateCommand updateCommand, boolean isOnline, BinaryContentDto profile) {
        return new UserDto(
                userId,
                updateCommand.username(),
                updateCommand.email(),
                profile,
                isOnline,
                Role.USER,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );
    }


    private UserDto createExpectedDto(UserCreateCommand command) {
        return createExpectedDto(UUID.randomUUID(), command, false);
    }


    private UserUpdateCommand createUpdateCommand() {
        return new UserUpdateCommand("changeUsername", "changePassword", "changeEmail@gmail.com");
    }


}
