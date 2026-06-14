package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {

    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;

    @Override
    public UserStatusResponse create(UserStatusCreateRequest request) {
        if (userRepository.findById(request.userId())==null) {
            throw new IllegalArgumentException("User not found");
        }
        if (userStatusRepository.findByUserId(request.userId()) != null) {
            throw new IllegalArgumentException("UserStatus already exists");
        }

        UserStatus userstatus = new UserStatus(request.userId(),request.lastSeenAt());
        userStatusRepository.save(userstatus);

        return toResponse(userstatus);
    }

    @Override
    public UserStatusResponse findById(UUID id) {
        return toResponse(userStatusRepository.findById(id));
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
        UserStatus userStatus = userStatusRepository.findById(request.id());
        userStatus.updateLastSeenAt(request.lastSeenAt());
        userStatusRepository.save(userStatus);

        return toResponse(userStatus);
    }

    @Override
    public UserStatusResponse findByUserId(UUID userId) {
        return toResponse(userStatusRepository.findByUserId(userId));
    }

    @Override
    public void delete(UUID id) {
        userStatusRepository.delete(id);
    }

    private UserStatusResponse toResponse(UserStatus userStatus) {
        return new UserStatusResponse(
                userStatus.getId(),
                userStatus.getUserId(),
                userStatus.getLastSeenAt(),
                userStatus.isOnline()
        );
    }
}
