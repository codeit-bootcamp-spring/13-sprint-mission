package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.BinaryContentRequest;
import com.sprint.mission.discodeit.dto.request.UserRequest;
import com.sprint.mission.discodeit.dto.response.UserResponse;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserMapper userMapper;
    private final BinaryContentStorage binaryContentStorage;

    @Override
    public UserResponse create(UserRequest dto, BinaryContentRequest profileDto) {
        log.debug("사용자 생성 시작: username={}, profileIncluded={}",
                dto.username(), profileDto != null);
        if (userRepository.existsByUsername(dto.username())) {
            log.warn("사용자 생성 실패 - 중복 username: {}", dto.username());
            throw new IllegalArgumentException("이미 사용 중인 이름입니다.");
        }
        if (userRepository.existsByEmail(dto.email())) {
            log.warn("사용자 생성 실패 - 중복 email: {}", dto.email());
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = new User(dto.username(), dto.email(), dto.password());

        if (profileDto != null && profileDto.fileName() != null && !profileDto.fileName().isBlank()) {
            BinaryContent binaryContent = new BinaryContent(
                    profileDto.fileName(),
                    profileDto.size(),
                    profileDto.contentType()
            );
            BinaryContent savedProfile = binaryContentRepository.save(binaryContent);

            binaryContentStorage.put(savedProfile.getId(), profileDto.bytes());

            user.updateProfile(savedProfile);
        }

        User savedUser = userRepository.save(user);

        UserStatus userStatus = new UserStatus(savedUser);
        savedUser.updateStatus(userStatus);
        userStatusRepository.save(userStatus);

        log.info("사용자 생성 완료: userId={}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponse> findById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    public UserResponse update(UUID id, UserRequest dto, BinaryContentRequest profileDto) {
        log.debug("사용자 수정 시작: userId={}, profileIncluded={}", id, profileDto != null);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        user.update(dto.username(), dto.password(), dto.email());

        if (profileDto != null && profileDto.fileName() != null && !profileDto.fileName().isBlank()) {
            BinaryContent newProfile = new BinaryContent(profileDto.fileName(), profileDto.size(), profileDto.contentType());
            BinaryContent savedProfile = binaryContentRepository.save(newProfile);
            binaryContentStorage.put(savedProfile.getId(), profileDto.bytes());
            user.updateProfile(savedProfile);
        }
        log.info("사용자 수정 완료: userId={}", id);
        return userMapper.toDto(user);
    }

    @Override
    public void delete(UUID id) {
        log.debug("사용자 삭제 시작: userId={}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

        userStatusRepository.deleteById(id);
        if (user.getProfile() != null) {
            binaryContentRepository.deleteById(user.getProfile().getId());
        }

        userRepository.delete(user);
        log.info("사용자 삭제 완료: userId={}", id);
    }
}