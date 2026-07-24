package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;
import lombok.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserStatusService implements UserStatusService {

    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserStatusDto create(CreateUserStatusRequest request) {
        UUID userId = request.userId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        Optional<UserStatus> existingUserStatus = userStatusRepository.findByUserId(userId);
        if (existingUserStatus.isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 유저입니다.");
        }

        Instant lastOnlineAt = request.lastOnlineAt();
        UserStatus userStatus = new UserStatus(user);
        userStatusRepository.save(userStatus);

        return UserStatusDto.from(userStatus);
    }

    @Override
    public UserStatusDto find(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("유저아이디가 없습니다.");
        }

        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저의 상태 정보가 없습니다."));

        return UserStatusDto.from(userStatus);
    }

    @Override
    public List<UserStatusDto> findAll() {
        return userStatusRepository.findAll().stream()
                .map(UserStatusDto::from)
                .toList();
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("삭제할 아이디가 없습니다.");
        }

        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 유저의 상태 정보가 없습니다."));

        userStatusRepository.delete(userStatus);
    }

    @Override
    @Transactional
    public UserStatusDto update(UUID id, UpdateUserStatusRequest request) {
        if(id == null) {
            throw new IllegalArgumentException("아이디는 필수입니다.");
        }

        if (request == null) {
            throw new IllegalArgumentException("업데이트할 유저가 없습니다.");
        }

        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("업데이트 유저의 정보가 없습니다."));

        userStatus.updateLastOnlineAt(request.lastOnlineTime());

        return UserStatusDto.from(userStatus);
    }

    @Override
    @Transactional
    public UserStatusDto updateByUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("유저 아이디는 필수입니다.");
        }

        UserStatus userStatus = userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("업데이트할 유저 정보가 없습니다."));

        userStatus.updateLastOnlineAt(Instant.now());

        return UserStatusDto.from(userStatus);
    }
}

