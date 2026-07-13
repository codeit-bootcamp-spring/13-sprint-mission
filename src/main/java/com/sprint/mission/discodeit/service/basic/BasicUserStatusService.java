package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.UserStatusDto;
import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateByUserIdRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BasicUserStatusService implements UserStatusService {

    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;
    private final UserStatusMapper userStatusMapper;

    @Override
    public UserStatusDto create(UserStatusCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NoSuchElementException("유저 ID: " + request.userId() + " 를 찾을 수 없습니다."));

        if (userStatusRepository.findByUserId(request.userId()).isPresent()) {
            throw new IllegalArgumentException("이미 해당 유저의 상태 정보가 존재합니다.");
        }

        UserStatus userStatus = UserStatus.create(user, request.lastActiveAt());
        UserStatus savedUserStatus = userStatusRepository.save(userStatus);
        user.updateStatus(savedUserStatus);

        log.info("UserStatus: {}가 생성됨.", savedUserStatus.getId());
        return userStatusMapper.toDto(savedUserStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatusDto find(UUID userStatusId) {
        UserStatus userStatus = userStatusRepository.findById(userStatusId)
                .orElseThrow(() -> new NoSuchElementException("유저 상태 ID: " + userStatusId + " 를 찾을 수 없습니다."));

        return userStatusMapper.toDto(userStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStatusDto> findAll() {
        return userStatusRepository.findAll().stream()
                .map(userStatusMapper::toDto)
                .toList();
    }

    @Override
    public UserStatusDto update(UserStatusUpdateRequest request) {
        UserStatus userStatus = userStatusRepository.findById(request.userStatusId())
                .orElseThrow(() -> new NoSuchElementException("유저 상태 ID: " + request.userStatusId() + " 를 찾을 수 없습니다."));

        userStatus.updateLastActiveAt(request.newLastActiveAt());

        log.info("UserStatus: {}가 수정됨.", userStatus.getId());
        return userStatusMapper.toDto(userStatus);
    }

    @Override
    public UserStatusDto updateByUserId(UserStatusUpdateByUserIdRequest request) {
        if (!userRepository.existsById(request.userId())) {
            throw new NoSuchElementException("유저 ID: " + request.userId() + " 를 찾을 수 없습니다.");
        }

        UserStatus userStatus = userStatusRepository.findByUserId(request.userId())
                .orElseThrow(() -> new NoSuchElementException("유저 ID: " + request.userId() + " 의 상태 정보를 찾을 수 없습니다."));

        userStatus.updateLastActiveAt(request.newLastActiveAt());

        log.info("UserStatus: {}가 유저 ID로 수정됨.", userStatus.getId());
        return userStatusMapper.toDto(userStatus);
    }

    @Override
    public void delete(UUID userStatusId) {
        if (!userStatusRepository.existsById(userStatusId)) {
            throw new NoSuchElementException("유저 상태 ID: " + userStatusId + " 를 찾을 수 없습니다.");
        }

        userStatusRepository.deleteById(userStatusId);
        log.info("UserStatus: {}가 삭제됨.", userStatusId);
    }
}