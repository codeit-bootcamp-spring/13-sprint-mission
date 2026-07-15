package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserStatusResponse;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

// UserStatus 기능을 실제로 구현하는 Service 클래스
@Service
@Transactional
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {

    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;

    // userId로 사용자의 상태 정보 조회
    @Override
    @Transactional(readOnly = true)
    public UserStatusResponse findByUserId(UUID userId) {
        validateUserExists(userId);

        UserStatus userStatus = userStatusRepository.findByUserId(userId);

        if (userStatus == null) {
            throw new IllegalArgumentException("조회할 사용자 상태 정보를 찾을 수 없습니다.");
        }

        return toResponse(userStatus);
    }

    // 사용자를 온라인 상태로 변경
    @Override
    public UserStatusResponse updateOnline(UUID userId) {
        validateUserExists(userId);

        UserStatus userStatus = userStatusRepository.findByUserId(userId);

        if (userStatus == null) {
            userStatus = new UserStatus(userId);
        }

        userStatus.updateOnline();

        UserStatus savedUserStatus = userStatusRepository.save(userStatus);

        return toResponse(savedUserStatus);
    }

    // 사용자를 오프라인 상태로 변경
    @Override
    public UserStatusResponse updateOffline(UUID userId) {
        validateUserExists(userId);

        UserStatus userStatus = userStatusRepository.findByUserId(userId);

        if (userStatus == null) {
            throw new IllegalArgumentException("오프라인으로 변경할 사용자 상태 정보를 찾을 수 없습니다.");
        }

        userStatus.updateOffline();

        UserStatus savedUserStatus = userStatusRepository.save(userStatus);

        return toResponse(savedUserStatus);
    }

    /*
     * API 명세 v1.2 기준
     * PATCH /api/users/{userId}/userStatus
     *
     * 요청 body:
     * {
     *   "newLastActiveAt": "2026-07-10T01:30:55.469015Z"
     * }
     *
     * newLastActiveAt 값을 실제 UserStatus.lastActiveAt에 반영한다.
     */
    @Override
    public UserStatusResponse updateLastActiveAt(
            UUID userId,
            Instant lastActiveAt
    ) {
        validateUserExists(userId);

        if (lastActiveAt == null) {
            lastActiveAt = Instant.now();
        }

        UserStatus userStatus = userStatusRepository.findByUserId(userId);

        if (userStatus == null) {
            userStatus = new UserStatus(userId);
        }

        userStatus.updateLastActiveAt(lastActiveAt);

        UserStatus savedUserStatus = userStatusRepository.save(userStatus);

        return toResponse(savedUserStatus);
    }

    // userId로 사용자의 상태 정보 삭제
    @Override
    public void deleteByUserId(UUID userId) {
        validateUserExists(userId);

        if (!userStatusRepository.existsByUserId(userId)) {
            throw new IllegalArgumentException("삭제할 사용자 상태 정보를 찾을 수 없습니다.");
        }

        userStatusRepository.deleteByUserId(userId);
    }

    private void validateUserExists(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 id는 null일 수 없습니다.");
        }

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
    }

    private UserStatusResponse toResponse(UserStatus userStatus) {
        return new UserStatusResponse(
                userStatus.getId(),
                userStatus.getCreatedAt(),
                userStatus.getUpdatedAt(),
                userStatus.getUserId(),
                userStatus.isOnline(),
                userStatus.getLastActiveAt()
        );
    }
}