package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateByUserIdRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusResponse;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;

    @Override
    public UserStatusResponse create(UserStatusCreateRequest request) {
        if(userRepository.findById(request.userId())==null) {
            throw new IllegalArgumentException("존재하지 않는 계정입니다.");
        }

        List<UserStatus> userStatuses = userStatusRepository.findAll();
        for (UserStatus userStatus : userStatuses) {
            if(userStatus.getUserId().equals(request.userId())) {
                throw new IllegalArgumentException("해당 계정의 상태 정보가 이미 존재합니다.");
            }
        }

        UserStatus userStatus = new UserStatus(request.userId());
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
    public UserStatusResponse update(UserStatusUpdateRequest request) {
        UserStatus userStatus = userStatusCheck(request.id());

        userStatus.updateActiveTime(request.updatedAt());
        userStatusRepository.save(userStatus);
        return returnResponse(userStatus);
    }

    @Override
    public UserStatusResponse updateByUserId(UserStatusUpdateByUserIdRequest request) {
        List<UserStatus> userStatus = userStatusRepository.findAll();
        for (UserStatus status : userStatus) {
            if(status.getUserId().equals(request.userId())) {
                status.updateActiveTime(request.updatedAt());
                userStatusRepository.save(status);
                return returnResponse(status);
            }
        }
        throw new IllegalArgumentException("해당 계정의 상태 정보가 존재하지 않습니다.");
    }

    @Override
    public void delete(UUID id) {
        UserStatus userStatus = userStatusCheck(id);
        userStatusRepository.delete(id);
    }

    private UserStatus userStatusCheck(UUID id) {
        UserStatus userStatus = userStatusRepository.findById(id);
        if(userStatus==null) {
            throw new IllegalArgumentException("존재하지 않는 계정 상태입니다.");
        }
        return userStatus;
    }

    private UserStatusResponse returnResponse(UserStatus userStatus) {
        return new UserStatusResponse(userStatus.getId(), userStatus.getUserId(), userStatus.getCreatedAt(), userStatus.getUpdatedAt(), userStatus.isOnline());
    }
}
