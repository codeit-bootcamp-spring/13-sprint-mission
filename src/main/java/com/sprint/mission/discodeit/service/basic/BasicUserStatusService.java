package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateByUserIdRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
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

    @Override
    @Transactional
    public UserStatusResponse create(UserStatusCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 계정입니다."));

        if(user.getStatus()!=null) {
            throw new IllegalArgumentException("해당 계정의 상태 정보가 이미 존재합니다.");
        }

        UserStatus userStatus = new UserStatus(user);
        userStatusRepository.save(userStatus);
        return returnResponse(userStatus);
    }

    @Override
    public UserStatusResponse find(UUID id) {
        UserStatus userStatus = userStatusCheck(id);
        return returnResponse(userStatus);
    }

    @Override
    public List<UserStatusResponse> findAll() {
        List<UserStatusResponse> responses = new ArrayList<>();
        List<UserStatus> userStatuses = userStatusRepository.findAll();

        for (UserStatus userStatus : userStatuses) {
            responses.add(returnResponse(userStatus));
        }
        return responses;
    }

    @Override
    @Transactional
    public UserStatusResponse update(UserStatusUpdateRequest request) {
        UserStatus userStatus = userStatusCheck(request.id());

        userStatus.updateActiveTime(request.updatedAt());
        userStatusRepository.save(userStatus);
        return returnResponse(userStatus);
    }

    @Override
    @Transactional
    public UserStatusResponse updateByUserId(UserStatusUpdateByUserIdRequest request) {
        UserStatus status = userStatusRepository.findByUser_Id(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("해당 계정의 상태 정보가 존재하지 않습니다."));

        status.updateActiveTime(request.updatedAt());
        userStatusRepository.save(status);
        return returnResponse(status);
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

    private UserStatusResponse returnResponse(UserStatus userStatus) {
        return new UserStatusResponse(userStatus.getId(), userStatus.getUser().getId(), userStatus.getCreatedAt(), userStatus.getUpdatedAt(), userStatus.isOnline());
    }
}
