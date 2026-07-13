package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final BinaryContentRepository contentRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentStorage binaryContentStorage;
    private final UserMapper userMapper;

    @Override
    public UserDto create(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자 이름입니다: " + request.username());
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.email());
        }

        User user = User.create(request.username(), request.email(), request.password());

        if (request.profile() != null) {
            BinaryContent profile = BinaryContent.create(
                    request.profile().fileName(),
                    request.profile().size(),
                    request.profile().contentType()
            );

            BinaryContent savedProfile = contentRepository.save(profile);
            binaryContentStorage.put(savedProfile.getId(), request.profile().bytes());
            user.updateProfile(savedProfile);
        }

        User savedUser = userRepository.save(user);

        UserStatus userStatus = UserStatus.create(savedUser, Instant.now());
        UserStatus savedUserStatus = userStatusRepository.save(userStatus);
        savedUser.updateStatus(savedUserStatus);

        log.info("유저: {}가 생성됨.", savedUser.getUsername());
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto find(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("유저 ID: " + userId + " 를 찾을 수 없습니다."));

        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    public UserDto update(UserUpdateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new NoSuchElementException("유저 ID: " + request.userId() + " 를 찾을 수 없습니다."));

        if (request.newUsername() != null
                && userRepository.existsByUsernameAndIdNot(request.newUsername(), request.userId())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자 이름입니다: " + request.newUsername());
        }

        if (request.newEmail() != null
                && userRepository.existsByEmailAndIdNot(request.newEmail(), request.userId())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.newEmail());
        }

        user.update(request.newUsername(), request.newEmail(), request.newPassword());

        if (request.newProfile() != null) {
            BinaryContent newProfile = BinaryContent.create(
                    request.newProfile().fileName(),
                    request.newProfile().size(),
                    request.newProfile().contentType()
            );

            BinaryContent oldProfile = user.getProfile();
            BinaryContent savedProfile = contentRepository.save(newProfile);
            binaryContentStorage.put(savedProfile.getId(), request.newProfile().bytes());
            user.updateProfile(savedProfile);

            if (oldProfile != null) {
                contentRepository.delete(oldProfile);
            }
        }

        log.info("유저: {}가 수정됨.", user.getUsername());
        return userMapper.toDto(user);
    }

    @Override
    public void delete(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("유저 ID: " + userId + " 를 찾을 수 없습니다."));

        if (user.getProfile() != null) {
            contentRepository.delete(user.getProfile());
        }

        userRepository.deleteById(userId);
        log.info("유저: {}가 삭제됨.", user.getUsername());
    }
}