package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor // 의존성 주입
@Service // Basic*Service 구현체를 Service 인터페이스의 Bean으로 등록
@Transactional(readOnly = true)
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final BinaryContentRepository contentRepository;
  private final UserStatusRepository statusRepository;
  private final UserMapper userMapper;

  @Transactional
  @Override
  public UserDto create(UserCreateRequest userCreateRequest,
      Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
    // username과 email 다른 유저와 다른지 중복 검사
    String username = userCreateRequest.getUsername();
    String email = userCreateRequest.getEmail();
    if (userRepository.existsByEmail(email)) { // API 스펙에 맞춰 추가
      throw new IllegalArgumentException("User with email " + email + " already exists");
    }
    if (userRepository.existsByUsername(username)) {
      throw new IllegalArgumentException("User with username " + username + " already exists");
    }
    // 프로필 이미지 있으면 등록
    BinaryContent savedProfile = optionalProfileCreateRequest
        .map(profileRequest -> {
          BinaryContent binaryContent = new BinaryContent(
              profileRequest.getFileName(),
              profileRequest.getContentType(),
              (long) profileRequest.getBytes().length,
              profileRequest.getBytes());
          return contentRepository.save(binaryContent);
        })
        .orElse(null);
    // 프로필 이미지 없으면 이는 비워두고 등록
    User user = new User(userCreateRequest.getUsername(), userCreateRequest.getEmail(),
        userCreateRequest.getPassword(), savedProfile);
    User savedUser = userRepository.save(user);
    // UserStatus를 같이 생성
    UserStatus userStatus = new UserStatus(savedUser, Instant.now());
    statusRepository.save(userStatus);
    return userMapper.toDto(savedUser);
  }

  @Override
  public UserDto find(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));
    return userMapper.toDto(user);
  }

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
        .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));
    String newUsername = userUpdateRequest.getNewUsername();
    String newEmail = userUpdateRequest.getNewEmail();
    if (userRepository.existsByEmail(newEmail)) { // API 스펙에 맞춰 추가
      throw new IllegalArgumentException("User with email " + newEmail + " already exists");
    }
    if (userRepository.existsByUsername(newUsername)) {
      throw new IllegalArgumentException("User with username " + newUsername + " already exists");
    }
    // 프로필 이미지 선택적으로 대체
    BinaryContent profile = optionalProfileCreateRequest
        .map(profileRequest -> {
          Optional.ofNullable(user.getProfile()).ifPresent(contentRepository::delete);

          BinaryContent binaryContent = new BinaryContent(
              profileRequest.getFileName(),
              profileRequest.getContentType(),
              (long) profileRequest.getBytes().length,
              profileRequest.getBytes());
          return contentRepository.save(binaryContent);
        })
        .orElse(null);
    // 기존 프로필 삭제
    String newPassword = userUpdateRequest.getNewPassword();
    user.update(newUsername, newEmail, newPassword, profile); // profileId 추가
    return userMapper.toDto(user);
  }

  @Transactional
  @Override
  public void delete(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));
    // 관련된 도메인도 같이 삭제
    if (user.getProfile() != null) {
      contentRepository.delete(user.getProfile());
    }
    userRepository.deleteById(userId);
  }
}
