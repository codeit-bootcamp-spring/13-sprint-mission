package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.command.userstatus.UserStatusCreateCommand;
import com.sprint.mission.discodeit.dto.command.userstatus.UserStatusUpdateCommand;
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

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicUserStatusService implements UserStatusService {

    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;
    private final UserStatusMapper userStatusMapper;


    //생성
    @Override
    public UserStatusDto create(UserStatusCreateCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 사용자 입니다."));
        boolean alreadyExists = userStatusRepository.findByUserId(command.userId()).isPresent();

        if (alreadyExists) {
            throw new IllegalArgumentException("이미 존재하는 UserStatus입니다.");
        }

        UserStatus userStatus = new UserStatus(user);
        userStatusRepository.save(userStatus);
        return userStatusMapper.toDto(userStatus);
    }

    //조회
    @Override
    @Transactional(readOnly = true)
    public UserStatusDto find(UUID id) {
        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 UserStatus입니다."));
        return userStatusMapper.toDto(userStatus);
    }

    //전체 조회
    @Override
    @Transactional(readOnly = true)
    public List<UserStatusDto> findAll() {
        return userStatusRepository.findAll()
                .stream()
                .map(userStatusMapper::toDto)
                .collect(Collectors.toList());
    }

    //수정(UserStatusId로 조회)
    @Override
    public UserStatusDto update(UUID id, UserStatusUpdateCommand command) {
        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 UserStatus입니다."));
        userStatus.updateLastActiveAt(command.newLastActiveAt());
        userStatusRepository.save(userStatus);
        return userStatusMapper.toDto(userStatus);
    }

    //수정(UserId로 조회)
    @Override
    public UserStatusDto updateByUserId(UUID userId, UserStatusUpdateCommand command) {
        UserStatus userStatus = userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 사용자 입니다."));

        userStatus.updateLastActiveAt(command.newLastActiveAt());
        userStatusRepository.save(userStatus);
        return userStatusMapper.toDto(userStatus);
    }

    //삭제
    @Override
    public void delete(UUID id) {
        userStatusRepository.findById(id)
                .orElseThrow(()-> new NoSuchElementException("존재하지 않는 UserStatus입니다."));

        userStatusRepository.deleteById(id);
    }
}
