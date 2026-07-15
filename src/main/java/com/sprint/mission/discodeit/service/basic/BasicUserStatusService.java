package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserStatusService implements UserStatusService {

    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;
    private final UserStatusMapper userStatusMapper;

    @Override
    @Transactional
    public UserStatusDto create(UserStatusCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (userStatusRepository.findByUserId(request.userId()).isPresent()) {
            throw new IllegalArgumentException("UserStatus already exists");
        }

        UserStatus userStatus = new UserStatus(user, request.lastSeenAt());
        userStatusRepository.save(userStatus);

        return userStatusMapper.toDto(userStatus);
    }

    @Override
    public UserStatusDto findById(UUID id) {
        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 상태입니다."));

        return userStatusMapper.toDto(userStatus);
    }

    @Override
    public Collection<UserStatusDto> findAll() {
        return userStatusRepository.findAll()
                .stream()
                .map(userStatusMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public UserStatusDto update(UserStatusUpdateRequest request) {
        UserStatus userStatus = userStatusRepository.findById(request.id())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 상태입니다."));

        userStatus.updateLastSeenAt(request.lastSeenAt());
        userStatusRepository.save(userStatus);

        return userStatusMapper.toDto(userStatus);
    }

    @Override
    @Transactional
    public UserStatusDto updateByUserId(UUID userId, UserStatusUpdateRequest request) {
        UserStatus userStatus = userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 상태입니다."));

        userStatus.updateLastSeenAt(request.lastSeenAt());
        userStatusRepository.save(userStatus);

        return userStatusMapper.toDto(userStatus);
    }

    @Override
    public UserStatusDto findByUserId(UUID userId) {
        UserStatus userStatus = userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 상태입니다."));

        return userStatusMapper.toDto(userStatus);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저 상태입니다."));

        userStatusRepository.delete(userStatus);
    }
}
