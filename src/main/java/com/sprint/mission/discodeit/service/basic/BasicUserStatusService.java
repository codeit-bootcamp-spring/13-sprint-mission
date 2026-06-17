package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateByUserIdRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusResponse;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {

    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;

    @Override
    public UserStatusResponse create(UserStatusCreateRequest request) {
        if (!userRepository.existsById(request.userId())) {
            throw new NoSuchElementException("유저 ID: " + request.userId() + " 를 찾을 수 없습니다.");
        }

        if (userStatusRepository.findByUserId(request.userId()).isPresent()) {
            throw new IllegalArgumentException("이미 해당 유저의 상태 정보가 존재합니다.");
        }

        UserStatus userStatus = new UserStatus(
                request.userId(),
                request.lastActiveAt()
        );
        UserStatus savedUserStatus = userStatusRepository.save(userStatus);

        log.info("UserStatus: {}가 생성됨.", savedUserStatus.getId());
        return toResponse(savedUserStatus);
    }

    @Override
    public UserStatusResponse find(UUID userStatusId) {
        UserStatus userStatus = userStatusRepository.findById(userStatusId)
                .orElseThrow(() -> new NoSuchElementException("유저 상태 ID: " + userStatusId + " 를 찾을 수 없습니다."));

        return toResponse(userStatus);
    }

    @Override
    public List<UserStatusResponse> findAll() {
        return userStatusRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UserStatusResponse update(UserStatusUpdateRequest request) {
        UserStatus userStatus = userStatusRepository.findById(request.userStatusId())
                .orElseThrow(() -> new NoSuchElementException("유저 상태 ID: " + request.userStatusId() + " 를 찾을 수 없습니다."));

        userStatus.updateLastActiveAt(request.newLastActiveAt());
        UserStatus savedUserStatus = userStatusRepository.save(userStatus);

        log.info("UserStatus: {}가 수정됨.", savedUserStatus.getId());
        return toResponse(savedUserStatus);
    }

    @Override
    public UserStatusResponse updateByUserId(UserStatusUpdateByUserIdRequest request) {
        if (!userRepository.existsById(request.userId())) {
            throw new NoSuchElementException("유저 ID: " + request.userId() + " 를 찾을 수 없습니다.");
        }

        UserStatus userStatus = userStatusRepository.findByUserId(request.userId())
                .orElseThrow(() -> new NoSuchElementException("유저 ID: " + request.userId() + " 의 상태 정보를 찾을 수 없습니다."));

        userStatus.updateLastActiveAt(request.newLastActiveAt());
        UserStatus savedUserStatus = userStatusRepository.save(userStatus);

        log.info("UserStatus: {}가 유저 ID로 수정됨.", savedUserStatus.getId());
        return toResponse(savedUserStatus);
    }

    @Override
    public void delete(UUID userStatusId) {
        if (!userStatusRepository.existsById(userStatusId)) {
            throw new NoSuchElementException("유저 상태 ID: " + userStatusId + " 를 찾을 수 없습니다.");
        }

        userStatusRepository.deleteById(userStatusId);
        log.info("UserStatus: {}가 삭제됨.", userStatusId);
    }

    private UserStatusResponse toResponse(UserStatus userStatus) {
        return new UserStatusResponse(
                userStatus.getId(),
                userStatus.getUserId(),
                userStatus.getLastActiveAt(),
                userStatus.getCreatedAt(),
                userStatus.getUpdatedAt(),
                userStatus.isOnline()
        );
    }
}
