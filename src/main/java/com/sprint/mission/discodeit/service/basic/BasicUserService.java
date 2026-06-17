package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final BinaryContentRepository contentRepository;
    private final UserStatusRepository userStatusRepository;

    @Override
    public UserResponse create(UserCreateRequest request) {

        if (existsByUsername(request.username())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자 이름입니다: " + request.username());
        }

        if (existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.email());
        }

        User user = new User(request.username(), request.email(), request.password());

        if (request.profile() != null) {
            BinaryContent profile = new BinaryContent(
                    request.profile().fileName(),
                    request.profile().size(),
                    request.profile().contentType(),
                    request.profile().bytes()
            );

            BinaryContent savedProfile = contentRepository.save(profile);
            user.updateProfileId(savedProfile.getId());
        }

        User savedUser = userRepository.save(user);

        UserStatus userStatus = new UserStatus(savedUser.getId(), Instant.now());
        userStatusRepository.save(userStatus);

        log.info("유저: {}가 생성됨.", savedUser.getUsername());
        return toResponse(savedUser);
    }

    private boolean existsByUsername(String username) {
        return userRepository.findAll().stream()
                .anyMatch(user -> user.getUsername().equals(username));
    }

    private boolean existsByEmail(String email) {
        return userRepository.findAll().stream()
                .anyMatch(user -> user.getEmail().equals(email));
    }

    @Override
    public UserResponse find(UUID userId) {
        return toResponse(userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("유저 ID: " + userId + " 를 찾을 수 없습니다.")));
    }

    @Override
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UserResponse update(UserUpdateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NoSuchElementException("유저 ID: " + request.userId() + " 를 찾을 수 없습니다."));

        if (request.newUsername() != null
                && existsByUsernameExceptSelf(request.newUsername(), request.userId())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자 이름입니다: " + request.newUsername());
        }

        if (request.newEmail() != null
                && existsByEmailExceptSelf(request.newEmail(), request.userId())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.newEmail());
        }

        user.update(request.newUsername(), request.newEmail(), request.newPassword());

        if (request.newProfile() != null) {
            BinaryContent newProfile = new BinaryContent(
                    request.newProfile().fileName(),
                    request.newProfile().size(),
                    request.newProfile().contentType(),
                    request.newProfile().bytes()
            );

            UUID oldProfileId = user.getProfileId();

            BinaryContent savedProfile = contentRepository.save(newProfile);
            user.updateProfileId(savedProfile.getId());

            if (oldProfileId != null) {
                contentRepository.deleteById(oldProfileId);
            }
        }

        User savedUser = userRepository.save(user);
        log.info("유저: {}가 수정됨.", savedUser.getUsername());
        return toResponse(savedUser);
    }

    private boolean existsByUsernameExceptSelf(String username, UUID userId) {
        return userRepository.findAll().stream()
                .anyMatch(user -> user.getUsername().equals(username)
                        && !user.getId().equals(userId));
    }

    private boolean existsByEmailExceptSelf(String email, UUID userId) {
        return userRepository.findAll().stream()
                .anyMatch(user -> user.getEmail().equals(email)
                        && !user.getId().equals(userId));
    }

    @Override
    public void delete(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("유저 ID: " + userId + " 를 찾을 수 없습니다."));

        if (user.getProfileId() != null) {
            contentRepository.deleteById(user.getProfileId());
        }

        userStatusRepository.findByUserId(userId)
                .ifPresent(userStatus -> userStatusRepository.deleteById(userStatus.getId()));

        userRepository.deleteById(userId);
        log.info("유저: {}가 삭제됨.", user.getUsername());
    }

    private UserResponse toResponse(User user) {
        boolean online = userStatusRepository.findByUserId(user.getId())
                .map(UserStatus::isOnline)
                .orElse(false);

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileId(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                online
        );
    }
}
