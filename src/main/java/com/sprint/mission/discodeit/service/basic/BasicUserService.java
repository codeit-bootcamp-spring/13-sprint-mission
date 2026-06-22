package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserListResponse;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.DuplicateUserException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
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
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Primary
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final UserStatusRepository userStatusRepository;

  @Override
  public UserResponse create(UserCreateRequest request, MultipartFile profile) {

    if (userRepository.findByUsername(request.username()).isPresent()) {
      throw new IllegalArgumentException("이미 존재하는 유저 이름입니다.");
    }

    if (userRepository.findByEmail(request.email()).isPresent()) {
      throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
    }

    UUID profileId = null;
    if (profile != null && !profile.isEmpty()) {
      BinaryContent binaryContent = BinaryContent.builder()
          .id(UUID.randomUUID())
          .createdAt(Instant.now())
          .updatedAt(Instant.now())
          .build();
      binaryContentRepository.save(binaryContent);
      profileId = binaryContent.getId();
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

    return UserResponse.from(user);
  }

  @Override
  public UserResponse findById(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("유저를 찾을 수 없습니다."));

    return UserResponse.from(user);

  }

  @Override
  public List<UserListResponse> findAll() {
    return userRepository.findAll().stream()
        .map(user -> {
          UserStatus status = userStatusRepository.findByUserId(user.getId()).orElse(null);
          boolean isOnLine = (status != null) && status.isOnline();
          return UserListResponse.from(user, isOnLine);
        })
        .toList();
  }

  @Override
  public UserResponse update(UUID id, UserUpdateRequest request, MultipartFile profile) {

    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));

    if (request.username() != null
        && !request.username().equals(user.getUsername())
        && userRepository.findByUsername(request.username()).isPresent()) {
      throw new DuplicateUserException("이미 존재하는 유저 이름입니다.");
    }

    if (request.email() != null
        && !request.email().equals(user.getEmail())
        && userRepository.findByEmail(request.email()).isPresent()) {
      throw new DuplicateUserException("이미 존재하는 이메일입니다.");
    }

    UUID currentProfileId = user.getProfileId();
    if (profile != null && !profile.isEmpty()) {
      if (currentProfileId != null) {
        binaryContentRepository.delete(currentProfileId);
      }

      BinaryContent newBinaryContent = BinaryContent.builder()
          .id(UUID.randomUUID())
          .createdAt(Instant.now())
          .updatedAt(Instant.now())
          .build();
      binaryContentRepository.save(newBinaryContent);
      currentProfileId = newBinaryContent.getId();
    }

    user.update(request.username(), request.email(), request.password(), currentProfileId);

    userRepository.save(user);

    return UserResponse.from(user);
  }

  @Override
  public void delete(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

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
