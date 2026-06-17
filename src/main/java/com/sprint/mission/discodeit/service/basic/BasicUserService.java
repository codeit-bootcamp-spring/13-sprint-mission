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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {
    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserStatusRepository userStatusRepository;

    @Override
    public UserResponse create(UserCreateRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 username 입니다.");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 email 입니다.");
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );

        user = userRepository.save(user);

        UserStatus userStatus = new UserStatus(
                user.getId()
        );

        userStatusRepository.save(userStatus);

        // 프로필 이미지가 있는 경우만 저장
        if (request.getData() != null) {

            BinaryContent profileImage = new BinaryContent(
                    user.getId(),
                    null,
                    request.getFileName(),
                    request.getContentType(),
                    request.getData()
            );

            binaryContentRepository.save(profileImage);
        }

        boolean online = userStatusRepository
                .findByUserId(user.getId())
                .map(UserStatus::isOnline)
                .orElse(false);

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                online
        );
    }

    @Override
    public UserResponse find(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "User with id " + userId + " not found"
                        )
                );

        boolean online = userStatusRepository
                .findByUserId(user.getId())
                .map(UserStatus::isOnline)
                .orElse(false);

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                online
        );
    }

    @Override
    public List<UserResponse> findAll() {

        return userRepository.findAll()
                .stream()
                .map(user -> {

                    boolean online = userStatusRepository
                            .findByUserId(user.getId())
                            .map(UserStatus::isOnline)
                            .orElse(false);

                    return new UserResponse(
                            user.getId(),
                            user.getUsername(),
                            user.getEmail(),
                            online
                    );
                })
                .toList();
    }

    @Override
    public UserResponse update(UserUpdateRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "User with id " + request.getUserId() + " not found"
                        )
                );

        user.update(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );

        user = userRepository.save(user);

        if (request.getData() != null) {

            binaryContentRepository.findByUserId(user.getId())
                    .ifPresent(profile ->
                            binaryContentRepository.deleteById(profile.getId())
                    );

            BinaryContent profileImage = new BinaryContent(
                    user.getId(),
                    null,
                    request.getFileName(),
                    request.getContentType(),
                    request.getData()
            );

            binaryContentRepository.save(profileImage);
        }

        boolean online = userStatusRepository
                .findByUserId(user.getId())
                .map(UserStatus::isOnline)
                .orElse(false);

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                online
        );
    }

    @Override
    public void delete(UUID userId) {

        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException(
                    "User with id " + userId + " not found"
            );
        }

        binaryContentRepository.findByUserId(userId)
                .ifPresent(profile ->
                        binaryContentRepository.deleteById(profile.getId())
                );

        userStatusRepository.findByUserId(userId)
                .ifPresent(status ->
                        userStatusRepository.deleteById(status.getId())
                );

        userRepository.deleteById(userId);
    }
}
