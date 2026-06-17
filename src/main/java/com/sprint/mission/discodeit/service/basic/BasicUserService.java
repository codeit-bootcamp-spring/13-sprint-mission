package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.UserService;
import lombok.*;
import org.springframework.stereotype.*;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository repository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public UserResponse create(UserRequest.CreateUserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("유저 생성 요청은 필수입니다.");
        }

        User user = new User(
                request.username(),
                request.email(),
                request.password()
        );

        repository.create(user);

        UserStatus userStatus = new UserStatus(user.getId());
        userStatusRepository.create(userStatus);

        return UserResponse.from(user, userStatus, null);
    }
    @Override
    public UserResponse find(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("유저 ID를 찾을 수가 없습니다.");
        }

        User user = repository.find(id);

        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저 ID입니다.");
        }

        UserStatus userStatus = userStatusRepository.findByUserId(id);
        BinaryContent profile = binaryContentRepository.findByUserId(id);

        return UserResponse.from(user, userStatus, profile);

    }

    @Override
    public List<UserResponse> findAll() {
        return repository.findAll().stream()
                .map (user -> {
                UserStatus userStatus = userStatusRepository.findByUserId(user.getId());
                BinaryContent profile = binaryContentRepository.findByUserId(user.getId());

            return UserResponse.from(user, userStatus, profile);
        }).toList();
    }

    @Override
    public UserResponse update(UUID id, UserRequest.UpdateUserRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("유저 ID는 필수입니다.");
        }

        if (request == null) {
            throw new IllegalArgumentException("유저 수정 요청은 필수입니다.");
        }

        User user = repository.find(id);

        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저 ID입니다.");
        }

        user.updateUserName(request.username());
        user.updateEmail(request.email());
        user.updatePassWord(request.password());

        repository.update(id, user);

        UserStatus userStatus = userStatusRepository.findByUserId(id);
        BinaryContent profile = binaryContentRepository.findByUserId(id);

        return UserResponse.from(user, userStatus, profile);
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("유저 ID는 필수입니다.");
        }

        if (!repository.exists(id)) {
            throw new IllegalArgumentException("존재하지 않는 유저 ID입니다.");
        }

        repository.delete(id);
    }
}