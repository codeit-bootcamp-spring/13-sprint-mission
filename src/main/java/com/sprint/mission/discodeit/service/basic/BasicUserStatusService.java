package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@AllArgsConstructor
public class BasicUserStatusService implements UserStatusService {
    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;
    // 의존성 주입

    @Override
    public UserStatusResponse create(UserStatusCreateRequest request) {
        // 관련된 User가 존재하지 않으면 예외 발생
        userRepository.findById(request.userId())
                .orElseThrow(()->new NoSuchElementException("존재하지 않는 사용자입니다."));
        // 같은 User와 관련된 객체가 이미 존재하면 예외 발생
        userStatusRepository.findById(request.userId())
                .ifPresent(userStatus->{throw new IllegalArgumentException("이미 존재하는 사용자입니다.");});
        UserStatus userStatus = new UserStatus(request.userId());
        userStatusRepository.save(userStatus);
        return UserStatusResponse.from(userStatus);
    }

    @Override
    public UserStatusResponse find(UUID userStatusId) { // id로 조회
        UserStatus userStatus = userStatusRepository.findById(userStatusId)
                .orElseThrow(() -> new NoSuchElementException(userStatusId + " 를 찾을 수 없습니다."));
        return UserStatusResponse.from(userStatus);
    }

    @Override
    public List<UserStatusResponse> findAll() { // 모든 객체 조회
        return userStatusRepository.findAll().stream()
                .map(UserStatusResponse::from)
                .toList();
    }

    @Override
    public UserStatusResponse update(UserStatusUpdateRequest request) {
        UserStatus userStatus = userStatusRepository.findById(request.userId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 아이디입니다."));
        userStatus.update(request.NewLastOnlineAt());
        userStatusRepository.save(userStatus);
        return UserStatusResponse.from(userStatus);
    }

    @Override
    public UserStatusResponse updateByUserId(UUID userId) { // userId로 특정 User의 객체를 업데이트
        UserStatus userStatus = userStatusRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 아이디입니다."));
        userStatus.update(Instant.now());
        userStatusRepository.save(userStatus);
        return UserStatusResponse.from(userStatus);
    }

    @Override
    public void delete(UUID userStatusId) {
        if (!userStatusRepository.existById(userStatusId)){
            throw new NoSuchElementException(userStatusId+" 를 찾을 수 없습니다.");
        }
        userStatusRepository.deleteById(userStatusId);
    }
}
