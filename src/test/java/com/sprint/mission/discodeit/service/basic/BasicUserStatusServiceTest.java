package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.dto.response.UserStatusResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusAlreadyExistException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.SseService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BasicUserStatusServiceTest {

    @Mock
    private UserStatusRepository userStatusRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserStatusMapper userStatusMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SseService sseService;

    @InjectMocks
    private BasicUserStatusService userStatusService;

    private UUID userId;
    private UUID userStatusId;
    private User user;
    private UserStatus userStatus;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userStatusId = UUID.randomUUID();

        user = new User(
                "codeit",
                "codeit@example.com",
                "encoded-password"
        );
        ReflectionTestUtils.setField(user, "id", userId);

        userStatus = new UserStatus(user);
        ReflectionTestUtils.setField(
                userStatus,
                "id",
                userStatusId
        );

        userResponse = new UserResponse(
                userId,
                "codeit",
                "codeit@example.com",
                true,
                null
        );
    }

    @Test
    void create_사용자_상태를_생성하고_사용자_갱신_이벤트를_전송한다() {
        UserStatusCreateRequest request =
                new UserStatusCreateRequest(userId);

        UserStatusResponse expected =
                userStatusResponse(userStatus.getLastActiveAt());

        given(userRepository.findById(userId))
                .willReturn(Optional.of(user));
        given(userStatusRepository.existsByUserId(userId))
                .willReturn(false);
        given(userStatusRepository.save(
                org.mockito.ArgumentMatchers.any(
                        UserStatus.class
                )
        )).willReturn(userStatus);
        given(userStatusMapper.toDto(userStatus))
                .willReturn(expected);
        given(userMapper.toDto(user))
                .willReturn(userResponse);

        UserStatusResponse actual =
                userStatusService.create(request);

        assertThat(actual).isEqualTo(expected);

        then(userStatusRepository).should()
                .save(
                        org.mockito.ArgumentMatchers.any(
                                UserStatus.class
                        )
                );
        then(sseService).should()
                .broadcast("users.updated", userResponse);
    }

    @Test
    void create_사용자가_없으면_실패한다() {
        UserStatusCreateRequest request =
                new UserStatusCreateRequest(userId);

        given(userRepository.findById(userId))
                .willReturn(Optional.empty());

        assertThatThrownBy(
                () -> userStatusService.create(request)
        ).isInstanceOf(UserNotFoundException.class);

        then(userStatusRepository).shouldHaveNoInteractions();
        then(userStatusMapper).shouldHaveNoInteractions();
        then(userMapper).shouldHaveNoInteractions();
        then(sseService).shouldHaveNoInteractions();
    }

    @Test
    void create_사용자_상태가_이미_존재하면_실패한다() {
        UserStatusCreateRequest request =
                new UserStatusCreateRequest(userId);

        given(userRepository.findById(userId))
                .willReturn(Optional.of(user));
        given(userStatusRepository.existsByUserId(userId))
                .willReturn(true);

        assertThatThrownBy(
                () -> userStatusService.create(request)
        ).isInstanceOf(UserStatusAlreadyExistException.class);

        then(userStatusMapper).shouldHaveNoInteractions();
        then(userMapper).shouldHaveNoInteractions();
        then(sseService).shouldHaveNoInteractions();
    }

    @Test
    void update_사용자_상태를_수정하고_사용자_갱신_이벤트를_전송한다() {
        Instant lastSeenAt = Instant.now();
        UserStatusUpdateRequest request =
                new UserStatusUpdateRequest(lastSeenAt);
        UserStatusResponse expected =
                userStatusResponse(lastSeenAt);

        given(userStatusRepository.findById(userStatusId))
                .willReturn(Optional.of(userStatus));
        given(userStatusMapper.toDto(userStatus))
                .willReturn(expected);
        given(userMapper.toDto(user))
                .willReturn(userResponse);

        UserStatusResponse actual =
                userStatusService.update(
                        userStatusId,
                        request
                );

        assertThat(actual).isEqualTo(expected);
        assertThat(userStatus.getLastActiveAt())
                .isEqualTo(lastSeenAt);

        then(sseService).should()
                .broadcast("users.updated", userResponse);
    }

    @Test
    void update_사용자_상태가_없으면_실패한다() {
        Instant lastSeenAt = Instant.now();
        UserStatusUpdateRequest request =
                new UserStatusUpdateRequest(lastSeenAt);

        given(userStatusRepository.findById(userStatusId))
                .willReturn(Optional.empty());

        assertThatThrownBy(
                () -> userStatusService.update(
                        userStatusId,
                        request
                )
        ).isInstanceOf(UserStatusNotFoundException.class);

        then(userStatusMapper).shouldHaveNoInteractions();
        then(userMapper).shouldHaveNoInteractions();
        then(sseService).shouldHaveNoInteractions();
    }

    @Test
    void updateByUserId_사용자_상태를_수정하고_사용자_갱신_이벤트를_전송한다() {
        Instant lastSeenAt = Instant.now();
        UserStatusUpdateRequest request =
                new UserStatusUpdateRequest(lastSeenAt);
        UserStatusResponse expected =
                userStatusResponse(lastSeenAt);

        given(userStatusRepository.findByUserId(userId))
                .willReturn(Optional.of(userStatus));
        given(userStatusMapper.toDto(userStatus))
                .willReturn(expected);
        given(userMapper.toDto(user))
                .willReturn(userResponse);

        UserStatusResponse actual =
                userStatusService.updateByUserId(
                        userId,
                        request
                );

        assertThat(actual).isEqualTo(expected);
        assertThat(userStatus.getLastActiveAt())
                .isEqualTo(lastSeenAt);

        then(sseService).should()
                .broadcast("users.updated", userResponse);
    }

    @Test
    void updateByUserId_사용자_상태가_없으면_실패한다() {
        Instant lastSeenAt = Instant.now();
        UserStatusUpdateRequest request =
                new UserStatusUpdateRequest(lastSeenAt);

        given(userStatusRepository.findByUserId(userId))
                .willReturn(Optional.empty());

        assertThatThrownBy(
                () -> userStatusService.updateByUserId(
                        userId,
                        request
                )
        ).isInstanceOf(UserStatusNotFoundException.class);

        then(userStatusMapper).shouldHaveNoInteractions();
        then(userMapper).shouldHaveNoInteractions();
        then(sseService).shouldHaveNoInteractions();
    }

    @Test
    void delete_사용자_상태를_삭제하고_사용자_갱신_이벤트를_전송한다() {
        user.updateStatus(userStatus);

        given(userStatusRepository.findById(userStatusId))
                .willReturn(Optional.of(userStatus));
        given(userMapper.toDto(user))
                .willReturn(userResponse);

        userStatusService.delete(userStatusId);

        then(userStatusRepository).should()
                .delete(userStatus);
        then(sseService).should()
                .broadcast("users.updated", userResponse);
    }

    @Test
    void delete_사용자_상태가_없으면_실패한다() {
        given(userStatusRepository.findById(userStatusId))
                .willReturn(Optional.empty());

        assertThatThrownBy(
                () -> userStatusService.delete(userStatusId)
        ).isInstanceOf(UserStatusNotFoundException.class);

        then(userStatusRepository).should()
                .findById(userStatusId);
        then(userMapper).shouldHaveNoInteractions();
        then(sseService).shouldHaveNoInteractions();
    }

    private UserStatusResponse userStatusResponse(
            Instant lastActiveAt
    ) {
        Instant createdAt = Instant.now();
        Instant updatedAt = Instant.now();

        return new UserStatusResponse(
                userStatusId,
                userId,
                lastActiveAt,
                createdAt,
                updatedAt,
                true
        );
    }
}