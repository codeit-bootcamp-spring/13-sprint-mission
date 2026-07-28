package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicUserStatusService implements UserStatusService {

    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;
    private final UserStatusMapper userStatusMapper;

    @Override
    public UserStatusResponse create(UserStatusCreateRequest dto) {
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        if (userStatusRepository.existsByUserId(dto.userId())) {
            throw new IllegalArgumentException("해당 유저의 접속 상태 객체가 이미 존재합니다.");
        }

        UserStatus userStatus = new UserStatus(user);
        user.updateStatus(userStatus);

        UserStatus savedUserStatus = userStatusRepository.save(userStatus);

        return userStatusMapper.toDto(savedUserStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserStatusResponse> find(UUID id) {
        return userStatusRepository.findById(id)
                .map(userStatusMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStatusResponse> findAll() {
        return userStatusRepository.findAll().stream()
                .map(userStatusMapper::toDto)
                .toList();
    }

    @Override
    public UserStatusResponse update(UUID id, UserStatusUpdateRequest dto) {
        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 접속 상태 정보를 찾을 수 없습니다."));

        userStatus.updateLastSeenAt(dto.lastSeenAt());

        return userStatusMapper.toDto(userStatus);
    }

    @Override
    public UserStatusResponse updateByUserId(UUID userId, UserStatusUpdateRequest dto) {
        UserStatus userStatus = userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저의 접속 상태 정보를 찾을 수 없습니다."));

        userStatus.updateLastSeenAt(dto.lastSeenAt());

        return userStatusMapper.toDto(userStatus);
    }

    @Override
    public void delete(UUID id) {
        userStatusRepository.deleteById(id);
    }
}