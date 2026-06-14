package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusResponse;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {

    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;

    @Override
    public UserStatusResponse create(UserStatusCreateRequest dto) {
        // [요구사항] 관련된 User 가 존재하지 않으면 예외를 발생시킵니다.
        // 💡 리아님의 UserRepository 반환 양식인 Optional 체인을 정석대로 활용합니다!
        userRepository.findById(dto.userId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        // [요구사항] 같은 User 와 관련된 객체가 이미 존재하면 예외를 발생시킵니다.
        boolean exists = userStatusRepository.findAll().stream()
                .anyMatch(us -> us.getUserId().equals(dto.userId()));
        if (exists) {
            throw new IllegalArgumentException("해당 유저의 접속 상태 객체가 이미 존재합니다.");
        }

        // 💡 엔티티 내부 규칙에 맞춰 생성해 줍니다.
        UserStatus userStatus = new UserStatus(dto.userId());
        userStatusRepository.create(userStatus);

        return new UserStatusResponse(
                userStatus.getId(), userStatus.getUserId(), userStatus.getUpdatedAt(),
                userStatus.getCreatedAt(), userStatus.getUpdatedAt()
        );
    }

    @Override
    public Optional<UserStatusResponse> find(UUID id) {
        // [요구사항] id로 조회합니다.
        return userStatusRepository.findById(id)
                .map(us -> new UserStatusResponse(
                        us.getId(), us.getUserId(), us.getUpdatedAt(),
                        us.getCreatedAt(), us.getUpdatedAt()
                ));
    }

    @Override
    public List<UserStatusResponse> findAll() {
        // [요구사항] 모든 객체를 조회합니다.
        return userStatusRepository.findAll().stream()
                .map(us -> new UserStatusResponse(
                        us.getId(), us.getUserId(), us.getUpdatedAt(),
                        us.getCreatedAt(), us.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public UserStatusResponse update(UUID id, UserStatusUpdateRequest dto) {
        // [요구사항] 수정 대상 객체의 id 파라미터, 수정할 값 파라미터 그룹화 반영
        UserStatus userStatus = userStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 접속 상태 정보를 찾을 수 없습니다."));

        // 💡 엔티티 내부의 시간 갱신 도구를 호출하세요 (예: updateLastSeenAt)
        userStatus.updateLastSeenAt(dto.lastSeenAt());
        userStatusRepository.update(userStatus);

        return new UserStatusResponse(
                userStatus.getId(), userStatus.getUserId(), userStatus.getUpdatedAt(),
                userStatus.getCreatedAt(), userStatus.getUpdatedAt()
        );
    }

    @Override
    public UserStatusResponse updateByUserId(UUID userId, UserStatusUpdateRequest dto) {
        // [요구사항] userId 로 특정 User의 객체를 찾아내어 업데이트합니다.
        UserStatus userStatus = userStatusRepository.findAll().stream()
                .filter(us -> us.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 유저의 접속 상태 정보를 찾을 수 없습니다."));

        userStatus.updateLastSeenAt(dto.lastSeenAt());
        userStatusRepository.update(userStatus);

        return new UserStatusResponse(
                userStatus.getId(), userStatus.getUserId(), userStatus.getUpdatedAt(),
                userStatus.getCreatedAt(), userStatus.getUpdatedAt()
        );
    }

    @Override
    public void delete(UUID id) {
        // [요구사항] id로 삭제합니다.
        userStatusRepository.delete(id);
    }
}
