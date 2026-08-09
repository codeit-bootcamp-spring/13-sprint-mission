package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor // 의존성 주입
@Service // Basic*Service 구현체를 Service 인터페이스의 Bean으로 등록
@Transactional(readOnly = true)
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final BinaryContentRepository contentRepository;
  private final UserStatusRepository statusRepository;
  private final UserMapper userMapper;
  private final BinaryContentStorage storage;

  @Transactional
  @Override
  public UserDto create(UserCreateRequest userCreateRequest,
      Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
    // username과 email 다른 유저와 다른지 중복 검사
    String username = userCreateRequest.getUsername();
    String email = userCreateRequest.getEmail();
    if (userRepository.existsByEmail(email)) { // API 스펙에 맞춰 추가
      log.warn("이미 사용 중인 사용자 이메일 {}", email);
      throw new UserAlreadyExistsException("중복된 email", email);
    }
    if (userRepository.existsByUsername(username)) {
      log.warn("이미 사용 중인 사용자 이름 {}", username);
      throw new UserAlreadyExistsException("중복된 username", username);
    }
    // 프로필 이미지 있으면 등록
    BinaryContent savedProfile = optionalProfileCreateRequest
        .map(profileRequest -> {
          BinaryContent binaryContent = new BinaryContent(
              profileRequest.getFileName(),
              profileRequest.getContentType(),
              (long) profileRequest.getBytes().length);
          BinaryContent saved = contentRepository.save(binaryContent);
          storage.put(saved.getId(), profileRequest.getBytes());
          return saved;
        })
        .orElse(null);
    // 프로필 이미지 없으면 이는 비워두고 등록
    User user = new User(username, email, // 중복된 호출 변수 사용
        userCreateRequest.getPassword(), savedProfile);
    // UserStatus를 같이 생성
    UserStatus userStatus = new UserStatus(user, Instant.now());
    User savedUser = userRepository.save(user);
    // 영속성 전이-> user만 저장해도 되는 것
    log.info("사용자 등록 userName={}, userEmail={}, savedProfile={}", username, email, savedProfile);
    return userMapper.toDto(savedUser);
  }

  @Transactional(readOnly = true)
  @Override
  public UserDto find(UUID userId) {
    log.debug("사용자 조회 userId={}", userId);
    return userRepository.findById(userId)
        .map(user -> userMapper.toDto(user))
        .orElseThrow(() -> new UserNotFoundException(userId));
  }

  @Transactional(readOnly = true)
  @Override
  public List<UserDto> findAll() {
    return userRepository.findAll().stream()
        .map(userMapper::toDto).toList();
  }

  @Transactional
  @Override
  public UserDto update(UUID userId, UserUpdateRequest userUpdateRequest,
      Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));
    String newUsername = userUpdateRequest.getNewUsername();
    String newEmail = userUpdateRequest.getNewEmail();
    if (userRepository.existsByEmail(newEmail) && !user.getEmail()
        .equals(newEmail)) { // API 스펙에 맞춰 추가
      log.warn("이미 사용 중인 사용자 이메일 {}", newEmail);
      throw new UserAlreadyExistsException("중복된 email", newEmail);
    }
    if (userRepository.existsByUsername(newUsername)) {
      log.warn("이미 존재하는 사용자 이름 {}", newUsername);
      throw new UserAlreadyExistsException("중복된 username", newUsername);
    }
    // 프로필 이미지 선택적으로 대체
    BinaryContent profile = optionalProfileCreateRequest
        .map(profileRequest -> {
          BinaryContent binaryContent = new BinaryContent(
              profileRequest.getFileName(),
              profileRequest.getContentType(),
              (long) profileRequest.getBytes().length);
          BinaryContent saved = contentRepository.save(binaryContent);
          storage.put(saved.getId(), profileRequest.getBytes());
          return saved;
        })
        .orElse(null);
    // 기존 프로필 삭제
    user.update(newUsername, newEmail, userUpdateRequest.getNewPassword(), profile); // profileId 추가
    log.info("사용자 정보 수정 userId={}, newUserName={}, newEmail={}, fileName={}", userId,
        newUsername, newEmail, profile.getFileName());
    return userMapper.toDto(user);
  }

  @Transactional
  @Override
  public void delete(UUID userId) {
    if (!userRepository.existsById(userId)) {
      log.warn("존재하지 않는 사용자 아이디 {}", userId);
      throw new UserNotFoundException(userId);
    }
    // user가 삭제되면 영속성 전이된 프로필 또한 삭제
    userRepository.deleteById(userId);
    log.info("사용자 삭제 userId={}", userId);
  }
}
