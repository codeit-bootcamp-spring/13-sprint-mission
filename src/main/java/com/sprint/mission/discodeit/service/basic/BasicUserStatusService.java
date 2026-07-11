package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {

    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;

    @Override
    public UserStatusResponse create(UserStatusCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (userStatusRepository.findByUserId(request.userId()).isPresent()) {
            throw new IllegalArgumentException("UserStatus already exists");
        }

        UserStatus userStatus = new UserStatus(user, request.lastSeenAt());
        userStatusRepository.save(userStatus);

        return toResponse(userStatus);
    }

    @Override
    public UserStatusResponse findById(UUID id) {
        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 상태입니다."));

        return toResponse(userStatus);
    }

    @Override
    public Collection<UserStatusResponse> findAll() {
        return userStatusRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UserStatusResponse update(UserStatusUpdateRequest request) {
        UserStatus userStatus = userStatusRepository.findById(request.id())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 상태입니다."));

        userStatus.updateLastSeenAt(request.lastSeenAt());
        userStatusRepository.save(userStatus);

        return toResponse(userStatus);
    }

    @Override
    public UserStatusResponse updateByUserId(UUID userId, UserStatusUpdateRequest request) {
        UserStatus userStatus = userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 상태입니다."));

        userStatus.updateLastSeenAt(request.lastSeenAt());
        userStatusRepository.save(userStatus);

        return toResponse(userStatus);
    }

    @Override
    public UserStatusResponse findByUserId(UUID userId) {
        UserStatus userStatus = userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 상태입니다."));

        return toResponse(userStatus);
    }

    @Override
    public void delete(UUID id) {
        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 상태입니다."));

        userStatusRepository.delete(userStatus);
    }

    private UserStatusResponse toResponse(UserStatus userStatus) {
        return new UserStatusResponse(
                userStatus.getId(),
                userStatus.getUser().getId(),
                userStatus.getLastSeenAt(),
                userStatus.isOnline()
        );
    }
}
