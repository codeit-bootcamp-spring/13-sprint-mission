package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {

    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;

    private UserStatusResponse toResponse(UserStatus userStatus){
        return new UserStatusResponse(
                userStatus.getUserId(),
                userStatus.getLastAccessedAt(),
                userStatus.isOnline()
        );
    }

    //생성
    @Override
    public UserStatusResponse create(UserStatusCreateRequest request) {
        userRepository.findById(request.userId())
                .orElseThrow(()-> new NoSuchElementException("존재하지 않는 사용자 입니다."));
        boolean alreadyExists = userStatusRepository.findByUserId(request.userId()).isPresent();

        if (alreadyExists) {
            throw new IllegalArgumentException("이미 존재하는 UserStatus입니다.");
        }

        UserStatus userStatus = new UserStatus(request.userId());
        userStatusRepository.save(userStatus);
        return toResponse(userStatus);
    }

    //조회
    @Override
    public UserStatusResponse find(UUID id) {
        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 UserStatus입니다."));
        return toResponse(userStatus);
    }

    //전체 조회
    @Override
    public List<UserStatusResponse> findAll() {
        return userStatusRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    //수정(UserStatusId로 조회)
    @Override
    public UserStatusResponse update(UUID id, UserStatusUpdateRequest request) {
        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 UserStatus입니다."));
        userStatus.updateLastAccessedAt(request.lastAccessedAt());
        userStatusRepository.save(userStatus);
        return toResponse(userStatus);
    }

    //수정(UserId로 조회)
    @Override
    public UserStatusResponse updateByUserId(UUID userId, UserStatusUpdateRequest request) {
        UserStatus userStatus = userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 사용자 입니다."));

        userStatus.updateLastAccessedAt(request.lastAccessedAt());
        userStatusRepository.save(userStatus);
        return toResponse(userStatus);
    }

    //삭제
    @Override
    public void delete(UUID id) {
        userStatusRepository.findById(id)
                .orElseThrow(()-> new NoSuchElementException("존재하지 않는 UserStatus입니다."));

        userStatusRepository.deleteById(id);
    }
}
