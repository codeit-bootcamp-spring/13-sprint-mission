package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.DuplicateUserException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final UserMapper userMapper;
    private final BinaryContentService binaryContentService;



    @Override
    @Transactional
    public UserDto create(UserCreateRequest createRequest) {
        log.info("Creating user: username={}, email={}", createRequest.username(), createRequest.email());

        validateUniqueUser(
                createRequest.username(),
                createRequest.email());

        BinaryContent profile = null;
        if (createRequest.profileBytes() != null) {
            profile = binaryContentService.createEntity(new BinaryContentCreateRequest(
                    createRequest.profileName(),
                    createRequest.profileContentType(),
                    createRequest.profileBytes()
            ));
        }

        User user = new User(
                createRequest.username(),
                createRequest.email(),
                createRequest.password(),
                profile
        );

        userRepository.save(user);

        UserStatus userStatus = new UserStatus(user, Instant.now());

        userStatusRepository.save(userStatus);
        return userMapper.toDto(user, userStatus);
    }

    @Override
    public UserDto findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        UserStatus userStatus = userStatusRepository.findByUserId(id).orElse(null);

        return userMapper.toDto(user, userStatus);
    }

    @Override
    public Collection<UserDto> findAll() {
        List<User> users = userRepository.findAll();

        Map<UUID, UserStatus> userStatusByUserId = userStatusRepository
                .findAllByUser_IdIn(users.stream().map(User::getId).toList())
                .stream()
                .collect(Collectors.toMap(
                        userStatus -> userStatus.getUser().getId(),
                        userStatus -> userStatus
                ));

        return users.stream()
                .map(user -> userMapper.toDto(user, userStatusByUserId.get(user.getId())))
                .toList();
    }

    @Override
    @Transactional
    public UserDto update ( UUID userId, UserUpdateRequest updateRequest) {
        log.info("Updating user: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        validateUniqueUserForUpdate(
                userId,
                updateRequest.username(),
                updateRequest.email());


        BinaryContent profile = user.getProfile();

        if (updateRequest.profileBytes() != null) {
            profile = binaryContentService.createEntity(new BinaryContentCreateRequest(
                    updateRequest.profileName(),
                    updateRequest.profileContentType(),
                    updateRequest.profileBytes()
            ));
        }

        user.renew(
                updateRequest.username(),
                updateRequest.email(),
                updateRequest.password(),
                profile
        );

        UserStatus userStatus = userStatusRepository.findByUserId(userId).orElse(null);
        return userMapper.toDto(user, userStatus);
        }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting user: userId={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        BinaryContent profile = user.getProfile();
        if (profile != null) {
            binaryContentService.delete(profile.getId());
        }

        userStatusRepository.findByUserId(id)
                .ifPresent(userStatusRepository::delete);

        userRepository.delete(user);
    }

    private void validateUniqueUser(String username, String email) {
        if (userRepository.findByName(username).isPresent()) {
            log.warn("Duplicate username detected: username={}", username);
            throw new DuplicateUserException("username", username);
        }

        if (userRepository.findByEmail(email).isPresent()) {
            log.warn("Duplicate email detected: email={}", email);
            throw new DuplicateUserException("email", email);
        }
    }

    private void validateUniqueUserForUpdate(UUID id, String username, String email) {
        userRepository.findByName(username)
                .filter(user -> !user.getId().equals(id))
                .ifPresent(user -> {
                    log.warn("Duplicate username detected during update: userId={}, username={}", id, username);
                    throw new DuplicateUserException("username", username);
                });

        userRepository.findByEmail(email)
                .filter(user -> !user.getId().equals(id))
                .ifPresent(user -> {
                    log.warn("Duplicate email detected during update: userId={}, email={}", id, email);
                    throw new DuplicateUserException("email", email);
                });
    }

}
