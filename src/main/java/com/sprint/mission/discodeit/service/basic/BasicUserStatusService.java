package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateByUserIdRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserStatusService implements UserStatusService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final UserStatusMapper userStatusMapper;

    @Override
    @Transactional
    public UserStatusDto create(UserStatusCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 계정입니다."));

        if(user.getStatus()!=null) {
            throw new IllegalArgumentException("해당 계정의 상태 정보가 이미 존재합니다.");
        }

        UserStatus userStatus = new UserStatus(user);
        userStatusRepository.save(userStatus);
        return userStatusMapper.toDto(userStatus);
    }

    @Override
    public UserStatusDto find(UUID id) {
        UserStatus userStatus = userStatusCheck(id);
        return userStatusMapper.toDto(userStatus);
    }

    @Override
    public List<UserStatusDto> findAll() {
        List<UserStatusDto> responses = new ArrayList<>();
        List<UserStatus> userStatuses = userStatusRepository.findAll();

        for (UserStatus userStatus : userStatuses) {
            responses.add(userStatusMapper.toDto(userStatus));
        }
        return responses;
    }

    @Override
    @Transactional
    public UserStatusDto update(UserStatusUpdateRequest request) {
        UserStatus userStatus = userStatusCheck(request.id());

        userStatus.updateActiveTime(request.updatedAt());
        userStatusRepository.save(userStatus);
        return userStatusMapper.toDto(userStatus);
    }

    @Override
    @Transactional
    public UserStatusDto updateByUserId(UserStatusUpdateByUserIdRequest request) {
        UserStatus userStatus = userStatusRepository.findByUser_Id(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("해당 계정의 상태 정보가 존재하지 않습니다."));

        userStatus.updateActiveTime(request.updatedAt());
        userStatusRepository.save(userStatus);
        return userStatusMapper.toDto(userStatus);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        UserStatus userStatus = userStatusCheck(id);
        userStatusRepository.delete(userStatus);
    }

    private UserStatus userStatusCheck(UUID id) {
        return userStatusRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 계정 상태입니다."));
    }
}
