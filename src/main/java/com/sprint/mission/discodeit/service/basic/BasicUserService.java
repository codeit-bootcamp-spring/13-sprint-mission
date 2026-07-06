package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
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

@RequiredArgsConstructor // 의존성 주입
@Service // Basic*Service 구현체를 Service 인터페이스의 Bean으로 등록
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final BinaryContentRepository contentRepository;
  private final UserStatusRepository statusRepository;

  @Override
  public UserDto create(UserCreateRequest userCreateRequest,
      Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
    // username과 email 다른 유저와 다른지 중복 검사
    String username = userCreateRequest.getUsername();
    String email = userCreateRequest.getEmail();
    if (userRepository.existByEmail(email)) { // API 스펙에 맞춰 추가
      throw new IllegalArgumentException("User with email " + email + " already exists");
    }
    if (userRepository.existByUsername(username)) {
      throw new IllegalArgumentException("User with username " + username + " already exists");
    }
    // 프로필 이미지 있으면 등록
    UUID profileId = optionalProfileCreateRequest
        .map(profileRequest -> {
          String fileName = profileRequest.getFileName();
          String contentType = profileRequest.getContentType();
          byte[] bytes = profileRequest.getBytes();
          BinaryContent binaryContent = new BinaryContent(fileName,
              contentType, (long) bytes.length, bytes);
          return contentRepository.save(binaryContent).getId();
        })
        .orElse(null);
    // 프로필 이미지 없으면 이는 비워두고 등록
    String password = userCreateRequest.getPassword();
    User user = new User(userCreateRequest.getUsername(), userCreateRequest.getEmail(), password,
        profileId);
    User createdUser = userRepository.save(user);
    // UserStatus를 같이 생성
    UserStatus userStatus = new UserStatus(createdUser.getId(), Instant.now());
    statusRepository.save(userStatus);
    return UserDto.from(createdUser, userStatus);
  }

  @Override
  public UserDto find(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));
    UserStatus userStatus = statusRepository.findByUserId(userId)
        .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));
    return UserDto.from(user, userStatus);
  }

  @Override
  public List<UserDto> findAll() {
    return userRepository.findAll().stream()
        .map(user -> {
          UserStatus userStatus = statusRepository.findByUserId(user.getId())
              .orElseThrow(
                  () -> new NoSuchElementException("User with id " + user.getId() + " not found"));
          return UserDto.from(user, userStatus);
        }).toList();
  }

  @Override
  public UserDto update(UUID userId, UserUpdateRequest userUpdateRequest,
      Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User with id " + userId + "  not found"));
    UserStatus userStatus = statusRepository.findByUserId(userId)
        .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));
    String newUsername = userUpdateRequest.getNewUsername();
    String newEmail = userUpdateRequest.getNewEmail();
    if (userRepository.existByEmail(newEmail)) { // API 스펙에 맞춰 추가
      throw new IllegalArgumentException("User with email " + newEmail + " already exists");
    }
    if (userRepository.existByUsername(newUsername)) {
      throw new IllegalArgumentException("User with username " + newUsername + " already exists");
    }
    // 프로필 이미지 선택적으로 대체
    UUID profileId = optionalProfileCreateRequest
        .map(profileRequest -> {
          Optional.ofNullable(user.getProfileId()).ifPresent(contentRepository::deleteById);
          String fileName = profileRequest.getFileName();
          String contentType = profileRequest.getContentType();
          byte[] bytes = profileRequest.getBytes();
          BinaryContent binaryContent = new BinaryContent(fileName,
              contentType, (long) bytes.length, bytes);
          return contentRepository.save(binaryContent).getId();
        })
        .orElse(null);
    // 기존 프로필 삭제
    String newPassword = userUpdateRequest.getNewPassword();
    user.update(newUsername, newEmail, newPassword, profileId); // profileId 추가
    userRepository.save(user);
    return UserDto.from(user, userStatus);
  }

  @Override
  public void delete(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));
    // 관련된 도메인도 같이 삭제
    if (user.getProfileId() != null) {
      contentRepository.deleteById(user.getProfileId());
    }
    statusRepository.findByUserId(userId)
        .ifPresent(userStatus -> statusRepository.deleteById(userStatus.getId()));
    userRepository.deleteById(userId);
  }
}
