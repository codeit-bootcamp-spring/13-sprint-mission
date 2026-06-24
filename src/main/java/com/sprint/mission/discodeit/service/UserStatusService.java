package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.CreateUserStatusRequest;
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
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        userStatusRepository.findByUserId(request.getUserId())
                .ifPresent(status -> {
                    throw new IllegalArgumentException("이미 UserStatus가 존재합니다.");
                });

        UserStatus status = new UserStatus( UUID.randomUUID(), request.getUserId() );
        userStatusRepository.save(status);

        return UserStatusResponse.from(status);
    }

    public UserStatusResponse find(UUID id) {
        UserStatus status = userStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("유저 상태를 찾을 수 없습니다."));

        return UserStatusResponse.from(status);
    }

    public List<UserStatusResponse> findAll() {
        return userStatusRepository.findAll()
                .stream()
                .map(UserStatusResponse::from)
                .toList();
    }

    public UserStatusResponse updateByUserId(UpdateUserStatusRequest request) {
        UserStatus status = userStatusRepository.findByUserId(request.getUserId())
                        .orElseThrow(() ->
                                new IllegalArgumentException("유저 상태를 찾을 수 없습니다."));

        status.updateLastSeen();
        userStatusRepository.save(status);

        return UserStatusResponse.from(status);
    }

    public void delete(UUID id) { userStatusRepository.delete(id); }

}
