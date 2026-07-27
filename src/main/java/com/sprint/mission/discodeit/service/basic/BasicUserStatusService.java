package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.command.*;
import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.mapper.*;
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
    private final UserStatusMapper userStatusMapper;

    @Override
    @Transactional
    public UserStatusDto create(CreateUserStatusCommand command) {
        UUID userId = command.userId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        Optional<UserStatus> existingUserStatus = userStatusRepository.findByUserId(userId);
        if (existingUserStatus.isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 유저입니다.");
        }

        UserStatus userStatus = new UserStatus(user);
        userStatus.updateLastActivityAt(command.lastActiveAt());
        userStatusRepository.save(userStatus);

        return userStatusMapper.toDto(userStatus);
    }

    @Override
    public UserStatusDto findByUserId(UUID userId)  {
        if (userId == null) {
            throw new IllegalArgumentException("유저아이디가 없습니다.");
        }

        UserStatus userStatus = userStatusRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저의 상태 정보가 없습니다."));

        return userStatusMapper.toDto(userStatus);
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
    public UserStatusDto update(UUID id, UpdateUserStatusCommand command) {
        if(id == null) {
            throw new IllegalArgumentException("아이디는 필수입니다.");
        }

        if (command == null) {
            throw new IllegalArgumentException("업데이트할 유저가 없습니다.");
        }

        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("업데이트 유저의 정보가 없습니다."));

        userStatus.updateLastActivityAt(command.lastOnlineTime());

        return userStatusMapper.toDto(userStatus);

    }

    @Override
    @Transactional
    public UserStatusDto updateByUserId(UUID userId, UpdateUserStatusCommand command) {
        if (userId == null) {
            throw new IllegalArgumentException("유저 아이디는 필수입니다.");
        }

        UserStatus userStatus = userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("업데이트할 유저 정보가 없습니다."));

        userStatus.updateLastActivityAt(Instant.now());

        return userStatusMapper.toDto(userStatus);
    }
}

