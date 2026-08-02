package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
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
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final UserMapper userMapper;
    private final BinaryContentRepository binaryContentRepository;
    private final BinaryContentStorage binaryContentStorage;

    @Transactional
    @Override
    public UserDto create(UserCreateRequest userCreateRequest,
                          Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
        String username = userCreateRequest.username();
        String email = userCreateRequest.email();

        // DEBUG: 상세한 진입 로그 (개발 환경에서만 출력)
        log.debug("사용자 생성 요청 - username={}, email={}", username, email);

        if (userRepository.existsByEmail(email)) {
            // WARN: 중복 요청처럼 비정상이지만 시스템 오류는 아닌 상황
            log.warn("사용자 생성 실패 - 이미 존재하는 email: {}", email);
            throw new UserAlreadyExistsException("email", email);
        }
        if (userRepository.existsByUsername(username)) {
            log.warn("사용자 생성 실패 - 이미 존재하는 username: {}", username);
            throw new UserAlreadyExistsException("username", username);
        }

        BinaryContent nullableProfile = optionalProfileCreateRequest
                .map(profileRequest -> {
                    String fileName = profileRequest.fileName();
                    String contentType = profileRequest.contentType();
                    byte[] bytes = profileRequest.bytes();
                    BinaryContent binaryContent = new BinaryContent(fileName, (long) bytes.length,
                            contentType);
                    binaryContentRepository.save(binaryContent);
                    binaryContentStorage.put(binaryContent.getId(), bytes);
                    // DEBUG: 프로필 이미지 업로드 상세 정보
                    log.debug("프로필 이미지 저장 완료 - binaryContentId={}, fileName={}",
                            binaryContent.getId(), fileName);
                    return binaryContent;
                })
                .orElse(null);

        String password = userCreateRequest.password();
        User user = new User(username, email, password, nullableProfile);
        Instant now = Instant.now();
        UserStatus userStatus = new UserStatus(user, now);

        userRepository.save(user);

        // INFO: 정상적인 비즈니스 이벤트 (운영 환경에서도 출력)
        log.info("사용자 생성 완료 - userId={}, username={}", user.getId(), username);
        return userMapper.toDto(user);
    }

    @Override
    public UserDto find(UUID userId) {
        log.debug("사용자 단건 조회 - userId={}", userId);
        return userRepository.findById(userId)
                .map(userMapper::toDto)
                .orElseThrow(() -> {
                    log.warn("사용자 조회 실패 - 존재하지 않는 userId={}", userId);
                    return new UserNotFoundException(userId);
                });
    }

    @Override
    public List<UserDto> findAll() {
        log.debug("전체 사용자 목록 조회");
        List<UserDto> users = userRepository.findAllWithProfileAndStatus()
                .stream()
                .map(userMapper::toDto)
                .toList();
        log.debug("전체 사용자 목록 조회 완료 - 총 {}명", users.size());
        return users;
    }

    @Transactional
    @Override
    public UserDto update(UUID userId, UserUpdateRequest userUpdateRequest,
                          Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
        log.debug("사용자 수정 요청 - userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("사용자 수정 실패 - 존재하지 않는 userId={}", userId);
                    return new NoSuchElementException("User with id " + userId + " not found");
                });

        String newUsername = userUpdateRequest.newUsername();
        String newEmail = userUpdateRequest.newEmail();
        if (userRepository.existsByEmail(newEmail)) {
            log.warn("사용자 수정 실패 - 이미 존재하는 email: {}", newEmail);
            throw new UserAlreadyExistsException("email", newEmail);
        }
        if (userRepository.existsByUsername(newUsername)) {
            log.warn("사용자 수정 실패 - 이미 존재하는 username: {}", newUsername);
            throw new UserAlreadyExistsException("username", newUsername);
        }

        BinaryContent nullableProfile = optionalProfileCreateRequest
                .map(profileRequest -> {
                    String fileName = profileRequest.fileName();
                    String contentType = profileRequest.contentType();
                    byte[] bytes = profileRequest.bytes();
                    BinaryContent binaryContent = new BinaryContent(fileName, (long) bytes.length,
                            contentType);
                    binaryContentRepository.save(binaryContent);
                    binaryContentStorage.put(binaryContent.getId(), bytes);
                    log.debug("프로필 이미지 업데이트 - binaryContentId={}", binaryContent.getId());
                    return binaryContent;
                })
                .orElse(null);

        String newPassword = userUpdateRequest.newPassword();
        user.update(newUsername, newEmail, newPassword, nullableProfile);

        log.info("사용자 수정 완료 - userId={}, newUsername={}", userId, newUsername);
        return userMapper.toDto(user);
    }

    @Transactional
    @Override
    public void delete(UUID userId) {
        log.debug("사용자 삭제 요청 - userId={}", userId);

        if (!userRepository.existsById(userId)) {
            log.warn("사용자 삭제 실패 - 존재하지 않는 userId={}", userId);
            throw new UserNotFoundException(userId);
        }

        userRepository.deleteById(userId);
        log.info("사용자 삭제 완료 - userId={}", userId);
    }
}