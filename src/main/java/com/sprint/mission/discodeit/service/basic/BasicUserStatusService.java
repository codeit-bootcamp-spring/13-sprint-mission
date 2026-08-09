package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.command.*;
import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.user.*;
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
        if (command == null) {
            throw new IllegalArgumentException("사용자 상태 생성 요청은 필수입니다.");
        }

        if (command.userId() == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        if (command.lastActiveAt() == null) {
            throw new IllegalArgumentException("마지막 활동 시간은 필수입니다.");
        }

        UUID userId = command.userId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Optional<UserStatus> existingUserStatus = userStatusRepository.findByUserId(userId);
        if (existingUserStatus.isPresent()) {
            throw new IllegalArgumentException("해당 사용자의 상태 정보가 이미 존재합니다.");
        }

        UserStatus userStatus = new UserStatus(user);
        userStatus.updateLastActivityAt(command.lastActiveAt());
        userStatusRepository.save(userStatus);

        return userStatusMapper.toDto(userStatus);
    }

    @Override
    public UserStatusDto findByUserId(UUID userId)  {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 상태 ID는 필수입니다.");
        }

        UserStatus userStatus = userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저의 상태 정보가 없습니다."));

        return userStatusMapper.toDto(userStatus);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("사용자 상태 ID는 필수입니다.");
        }

        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 사용자 상태 정보가 없습니다."));

        userStatusRepository.delete(userStatus);
    }

    @Override
    @Transactional
    public UserStatusDto update(UUID id, UpdateUserStatusCommand command) {
        if(id == null) {
            throw new IllegalArgumentException("사용자 상태 ID는 필수입니다.");
        }

        if (command == null) {
            throw new IllegalArgumentException("사용자 상태 수정 요청은 필수입니다.");
        }

        if (command.lastOnlineTime() == null) {
            throw new IllegalArgumentException(
                    "마지막 접속 시간은 필수입니다."
            );
        }

        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("수정할 사용자 상태 정보가 없습니다."));

        userStatus.updateLastActivityAt(command.lastOnlineTime());

        return userStatusMapper.toDto(userStatus);

    }

    @Override
    @Transactional
    public UserStatusDto updateByUserId(UUID userId, UpdateUserStatusCommand command) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }

        if (command == null) {
            throw new IllegalArgumentException("사용자 상태 수정 요청은 필수입니다.");
        }

        if (command.lastOnlineTime() == null) {
            throw new IllegalArgumentException("마지막 접속 시간은 필수입니다.");
        }

        UserStatus userStatus = userStatusRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "수정할 사용자 상태 정보가 없습니다."
                        )
                );

        userStatus.updateLastActivityAt(command.lastOnlineTime());

        return userStatusMapper.toDto(userStatus);
    }
}

