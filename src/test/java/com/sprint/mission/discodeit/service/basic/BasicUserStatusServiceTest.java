package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusNotFoundByIdException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusNotFoundByUserIdException;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BasicUserStatusServiceTest {

    @Mock
    private UserStatusRepository userStatusRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserStatusMapper userStatusMapper;

    @InjectMocks
    private BasicUserStatusService userStatusService;

    @Nested
    @DisplayName("UserStatus 생성")
    class CreateUserStatus {

        @Test
        @DisplayName("사용자가 존재하고 상태가 중복되지 않으면 생성한다")
        void create_success() {
            // given
            UUID userId = UUID.randomUUID();
            UserStatusCreateRequest request =
                    new UserStatusCreateRequest(userId);

            User user = mock(User.class);
            UserStatus savedUserStatus = mock(UserStatus.class);
            UserStatusDto expected = mock(UserStatusDto.class);

            given(userRepository.findById(userId))
                    .willReturn(Optional.of(user));

            given(userStatusRepository.existsByUserId(userId))
                    .willReturn(false);

            given(userStatusRepository.save(any(UserStatus.class)))
                    .willReturn(savedUserStatus);

            given(userStatusMapper.toDto(savedUserStatus))
                    .willReturn(expected);

            // when
            UserStatusDto result = userStatusService.createUserStatus(request);

            // then
            assertThat(result)
                    .isSameAs(expected);

            then(userRepository).should()
                    .findById(userId);

            then(userStatusRepository).should()
                    .existsByUserId(userId);

            then(userStatusRepository).should()
                    .save(any(UserStatus.class));

            then(userStatusMapper).should()
                    .toDto(savedUserStatus);
        }

        @Test
        @DisplayName("사용자가 존재하지 않으면 예외가 발생한다")
        void create_fail_no_user() {
            // given
            UUID userId = UUID.randomUUID();
            UserStatusCreateRequest request = new UserStatusCreateRequest(userId);

            given(userRepository.findById(userId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userStatusService.createUserStatus(request))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("이미 UserStatus가 존재하면 예외가 발생한다")
        void create_fail_duplicate() {
            // given
            UUID userId = UUID.randomUUID();
            UserStatusCreateRequest request = new UserStatusCreateRequest(userId);

            User user = mock(User.class);

            given(userRepository.findById(userId))
                    .willReturn(Optional.of(user));

            given(userStatusRepository.existsByUserId(userId))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> userStatusService.createUserStatus(request))
                    .isInstanceOf(UserStatusAlreadyExistsException.class);

            then(userStatusRepository).should(never())
                    .save(any(UserStatus.class));
        }
    }

    @Nested
    @DisplayName("UserStatus 단건 조회")
    class GetUserStatus {

        @Test
        @DisplayName("UserStatus가 존재하면 DTO로 반환한다")
        void get_success() {
            // given
            UUID userStatusId = UUID.randomUUID();

            UserStatus userStatus = mock(UserStatus.class);
            UserStatusDto expected = mock(UserStatusDto.class);

            given(userStatusRepository.findById(userStatusId))
                    .willReturn(Optional.of(userStatus));

            given(userStatusMapper.toDto(userStatus))
                    .willReturn(expected);

            // when
            UserStatusDto result = userStatusService.getUserStatus(userStatusId);

            // then
            assertThat(result)
                    .isSameAs(expected);

            then(userStatusRepository).should()
                    .findById(userStatusId);

            then(userStatusMapper).should()
                    .toDto(userStatus);
        }

        @Test
        @DisplayName("UserStatus가 존재하지 않으면 예외가 발생한다")
        void get_fail() {
            // given
            UUID userStatusId = UUID.randomUUID();

            given(userStatusRepository.findById(userStatusId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userStatusService.getUserStatus(userStatusId))
                    .isInstanceOf(UserStatusNotFoundByIdException.class);
        }
    }

    @Nested
    @DisplayName("UserStatus 목록 조회")
    class GetUserStatuses {

        @Test
        @DisplayName("저장된 UserStatus 목록을 DTO 목록으로 반환한다")
        void get_success() {
            // given
            UserStatus userStatus1 = mock(UserStatus.class);
            UserStatus userStatus2 = mock(UserStatus.class);

            UserStatusDto dto1 = mock(UserStatusDto.class);
            UserStatusDto dto2 = mock(UserStatusDto.class);

            given(userStatusRepository.findAll())
                    .willReturn(List.of(userStatus1, userStatus2));

            given(userStatusMapper.toDto(userStatus1))
                    .willReturn(dto1);

            given(userStatusMapper.toDto(userStatus2))
                    .willReturn(dto2);

            // when
            List<UserStatusDto> result = userStatusService.getUserStatuses();

            // then
            assertThat(result)
                    .containsExactly(dto1, dto2);

            then(userStatusRepository).should()
                    .findAll();
            then(userStatusMapper).should()
                    .toDto(userStatus1);
            then(userStatusMapper).should()
                    .toDto(userStatus2);
        }

        @Test
        @DisplayName("저장된 UserStatus가 없으면 빈 목록을 반환한다")
        void get_fail() {
            // given
            given(userStatusRepository.findAll())
                    .willReturn(List.of());

            // when
            List<UserStatusDto> result = userStatusService.getUserStatuses();

            // then
            assertThat(result)
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("UserStatus 수정")
    class UpdateUserStatus {

        @Test
        @DisplayName("사용자 ID에 해당하는 상태가 존재하면 마지막 접속 시간을 수정한다")
        void update_success() {
            // given
            UUID userId = UUID.randomUUID();

            UserStatusUpdateRequest request = mock(UserStatusUpdateRequest.class);

            UserStatus userStatus = mock(UserStatus.class);
            UserStatusDto expected = mock(UserStatusDto.class);

            given(userStatusRepository.findByUserId(userId))
                    .willReturn(Optional.of(userStatus));

            given(userStatusMapper.toDto(userStatus))
                    .willReturn(expected);

            // when
            UserStatusDto result = userStatusService.updateUserStatusByUserId(
                    userId,
                    request
            );

            // then
            assertThat(result)
                    .isSameAs(expected);

            then(userStatus).should()
                    .updateLastActiveAt();

            then(userStatusMapper).should()
                    .toDto(userStatus);
        }

        @Test
        @DisplayName("사용자 ID에 해당하는 상태가 없으면 예외가 발생한다")
        void update_fail() {
            // given
            UUID userId = UUID.randomUUID();

            UserStatusUpdateRequest request = mock(UserStatusUpdateRequest.class);

            given(userStatusRepository.findByUserId(userId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userStatusService.updateUserStatusByUserId(
                    userId,
                    request
            )).isInstanceOf(UserStatusNotFoundByUserIdException.class);
        }
    }

    @Nested
    @DisplayName("UserStatus 삭제")
    class DeleteUserStatus {

        @Test
        @DisplayName("UserStatus가 존재하면 삭제한다")
        void delete_success() {
            // given
            UUID userStatusId = UUID.randomUUID();

            UserStatus userStatus = mock(UserStatus.class);

            given(userStatusRepository.findById(userStatusId))
                    .willReturn(Optional.of(userStatus));

            given(userStatus.getId())
                    .willReturn(userStatusId);

            // when
            userStatusService.deleteUserStatus(userStatusId);

            // then
            then(userStatusRepository).should()
                    .deleteById(userStatusId);
        }

        @Test
        @DisplayName("UserStatus가 존재하지 않으면 예외가 발생한다")
        void delete_fail() {
            // given
            UUID userStatusId = UUID.randomUUID();

            given(userStatusRepository.findById(userStatusId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userStatusService.deleteUserStatus(userStatusId))
                    .isInstanceOf(UserStatusNotFoundByIdException.class);

            then(userStatusRepository).should(never())
                    .deleteById(userStatusId);
        }
    }

}