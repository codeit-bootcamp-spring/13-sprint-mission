package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Primary
public class BasicUserStatusService implements UserStatusService {

    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;

    public BasicUserStatusService(UserStatusRepository userStatusRepository, UserRepository userRepository) {
        this.userStatusRepository = userStatusRepository;
        this.userRepository = userRepository;
    }


    @Override
    public UserStatusResponse create(UserStatusCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() ->new IllegalArgumentException("존재하지 않는 유저입니다."));


        boolean isAlreadyExist = userStatusRepository.findAll().stream()
                .anyMatch(us -> us.isUser(request.userId()));

        if (isAlreadyExist) {
            throw new IllegalArgumentException("이미 상태 정보가 존재하는 유저입니다.");
        }

        UserStatus userStatus = UserStatus.builder()
                .id(UUID.randomUUID())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .user(user)
                .lastActiveAt(Instant.now())
                .build();

        userStatusRepository.save(userStatus);

        return convertToResponse(userStatus);
    }

    @Override
    public UserStatusResponse find(UUID id) {
        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 상태 정보입니다."));

        return convertToResponse(userStatus);
    }

    @Override
    public List<UserStatusResponse> findAll() {
        return userStatusRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserStatusResponse update(UserStatusUpdateRequest request) {
        UserStatus userStatus = userStatusRepository.findById(request.id())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 상태 정보입니다."));

        userStatus.updateActiveTime();
        userStatusRepository.save(userStatus);

        return convertToResponse(userStatus);
    }

    @Override
    public UserStatusResponse updateByUserId(UUID userId) {
        UserStatus userStatus = userStatusRepository.findAll().stream()
                .filter(us -> us.isUser(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 상태 정보입니다."));

        userStatus.updateActiveTime();
        userStatusRepository.save(userStatus);

        return convertToResponse(userStatus);
    }

    @Override
    public void delete(UUID id) {
        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 상태 정보입니다."));

        userStatusRepository.delete(id);
    }

    private UserStatusResponse convertToResponse(UserStatus userStatus) {
        String currentStatus = userStatus.isOnline() ? "online" : "offline";

        return new UserStatusResponse(
                userStatus.getId(),
                userStatus.getCreatedAt(),
                userStatus.getUpdatedAt(),
                userStatus.getUser().getId(),
                currentStatus
        );
    }
}
