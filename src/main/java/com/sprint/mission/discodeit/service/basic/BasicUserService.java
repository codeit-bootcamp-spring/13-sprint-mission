package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Primary
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserStatusRepository userStatusRepository;

    @Override
    public UserResponse create(UserCreateRequest request) {

        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 유저 이름입니다.");
        }

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        UUID profileId = request.profileId();
        if (profileId != null && binaryContentRepository.findById(profileId).isEmpty()) {
            throw new NoSuchElementException("존재하지 않는 프로필 이미지입니다.");
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .username(request.username())
                .email(request.email())
                .password(request.password())
                .profileId(profileId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        userRepository.save(user);

        UserStatus userStatus = UserStatus.builder()
                .id(UUID.randomUUID())
                .user(user)
                .lastActiveAt(Instant.now())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        userStatusRepository.save(userStatus);

        return UserResponse.from(user, userStatus.isOnline());
    }

    @Override
    public UserResponse findById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("유저를 찾을 수 없습니다."));

        UserStatus userStatus = userStatusRepository.findByUserId(userId).orElse(null);
        boolean isOnLine = (userStatus != null) && userStatus.isOnline();

        return UserResponse.from(user, isOnLine);

    }

    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map( user -> {
                    UserStatus status = userStatusRepository.findByUserId(user.getId()).orElse(null);
                    boolean isOnLine = (status != null) && status.isOnline();
                    return UserResponse.from(user, isOnLine);
                })
                .toList();
    }

    @Override
    public UserResponse update(UserUpdateRequest request) {

        User user = userRepository.findById(request.id())
                .orElseThrow(() -> new NoSuchElementException("유저를 찾을 수 없습니다."));

        if (request.username() != null
                && !request.username().equals(user.getUsername())
                && userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 유저 이름입니다.");
        }

        if (request.email() != null
                && !request.email().equals(user.getEmail())
                && userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        user.update(request.username(), request.email(), request.password(), request.profileId());

        userRepository.save(user);

        UserStatus userStatus = userStatusRepository.findByUserId(user.getId()).orElse(null);
        boolean isOnLine = (userStatus != null) && userStatus.isOnline();

        return UserResponse.from(user, isOnLine);
    }

    @Override
    public void delete(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException(userId + " 유저를 찾을 수 없습니다."));


        if (user.getProfileId() != null) {
            binaryContentRepository.delete(user.getProfileId());
        }
        UserStatus userStatus = userStatusRepository.findByUserId(userId).orElse(null);
        if (userStatus != null) {
            userStatusRepository.delete(userStatus.getId());
        }

        userRepository.delete(userId);

    }
}
