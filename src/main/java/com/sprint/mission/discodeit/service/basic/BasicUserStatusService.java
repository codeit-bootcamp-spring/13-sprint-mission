package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateByUserIdRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusNotFoundException;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(()->new UserNotFoundException(request.userId()));

        if(user.getStatus()!=null) {
            throw new UserStatusAlreadyExistsException(user.getId());
        }

        UserStatus userStatus = new UserStatus(user);
        userStatusRepository.save(userStatus);
        return userStatusMapper.toDto(userStatus);
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
                .orElseThrow(() -> new UserStatusNotFoundException(request.userId()));

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
                .orElseThrow(()->new UserStatusNotFoundException(id));
    }
}
