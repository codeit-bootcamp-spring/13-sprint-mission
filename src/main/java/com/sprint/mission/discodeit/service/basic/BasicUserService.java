package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserRequest;
import com.sprint.mission.discodeit.dto.request.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
//    private final UserStatusRepository userStatusRepository;
//    private final BinaryContentRepository binaryContentRepository;

    @Override
    public UserResponse create(UserRequest dto) {
        // [요구사항] username과 email 중복 검사 로직 수행
        User user = new User(dto.username(), dto.email(),  dto.password());
        userRepository.create(user);
        // [요구사항] UserStatus(접속 상태) 상자도 세트로 같이 생성해서 저장
        // [요구사항] 선택적으로 프로필 이미지를 같이 등록
        return new UserResponse(user.getId(),user.getUsername(),user.getEmail(),true);
    }

    @Override
    public Optional<UserResponse> findById(UUID id) {
        return userRepository.findById(id)
                .map(user -> new UserResponse
                        (user.getId(),user.getUsername(),user.getEmail(),true));
    }

    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(user -> new UserResponse
                (user.getId(), user.getUsername(), user.getEmail(), true))
                .toList();
    }

    @Override
    public UserResponse update(UUID id, UserRequest dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
        user.update(dto.username(), dto.email(), dto.password());
        if (dto.profileImageName() != null && !dto.profileImageName().isEmpty()) {
            user.updateProfileId(UUID.randomUUID());
        }
        userRepository.update(user);
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), true);
    }

    @Override
    public void delete(UUID id) {
        userRepository.delete(id);
    }

}

/*
UserService 고도화

create
[ ] 선택적으로 프로필 이미지를 같이 등록할 수 있습니다.
[ ] DTO를 활용해 파라미터를 그룹화합니다.
유저를 등록하기 위해 필요한 파라미터, 프로필 이미지를 등록하기 위해 필요한 파라미터 등
[ ] username과 email은 다른 유저와 같으면 안됩니다.
[ ] UserStatus를 같이 생성합니다.

find, findAll
DTO를 활용하여:
[ ] 사용자의 온라인 상태 정보를 같이 포함하세요.
[ ] 패스워드 정보는 제외하세요.

update
[ ] 선택적으로 프로필 이미지를 대체할 수 있습니다.
[ ] DTO를 활용해 파라미터를 그룹화합니다.
수정 대상 객체의 id 파라미터, 수정할 값 파라미터

delete
[ ] 관련된 도메인도 같이 삭제합니다.
BinaryContent(프로필), UserStatus

의존성
같은 레이어 간 의존성 주입은 순환 참조 방지를 위해 지양합니다. 다른 Service 대신 필요한 Repository 의존성을 주입해보세요.
 */