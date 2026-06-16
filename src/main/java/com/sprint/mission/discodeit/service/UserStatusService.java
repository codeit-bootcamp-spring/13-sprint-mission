package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.CreateUserStatusRequest;
import com.sprint.mission.discodeit.dto.request.UpdateUserStatusByUserIdRequest;
import com.sprint.mission.discodeit.dto.request.UpdateUserStatusRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserStatusService {

    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;

    public UserStatusResponse create(CreateUserStatusRequest request) {
        User user = userRepository.findById(request.userId());

        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
        boolean exists = userStatusRepository.findById(request.userId()) != null;

        if (exists) {
            throw new IllegalArgumentException("이미 UserStatus가 존재합니다.");
        }

        UserStatus status = new UserStatus( UUID.randomUUID(), request.userId() );
        userStatusRepository.save(status);

        return UserStatusResponse.from(status);
    }

    public UserStatusResponse find(UUID id) {
        UserStatus status = userStatusRepository.findById(id);

        return UserStatusResponse.from(status);
    }

    public List<UserStatusResponse> findAll() {
        return userStatusRepository.findAll()
                .stream()
                .map(UserStatusResponse::from)
                .toList();
    }

    public UserStatusResponse update(UpdateUserStatusRequest request) {
        UserStatus status = userStatusRepository.findById(request.id());
        status.updateLastSeen();
        userStatusRepository.save(status);

        return UserStatusResponse.from(status);
    }

    public UserStatusResponse updateByUserId(UpdateUserStatusByUserIdRequest request) {
        UserStatus status = userStatusRepository.findAll()
                .stream()
                .filter(s -> s.getUserId().equals(request.userId()))
                .findFirst()
                .orElseThrow();

        status.updateLastSeen();
        userStatusRepository.save(status);

        return UserStatusResponse.from(status);
    }

    public void delete(UUID id) { userStatusRepository.delete(id); }

}
