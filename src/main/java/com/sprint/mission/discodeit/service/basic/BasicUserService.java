package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.DuplicateUserException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Primary
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final UserStatusRepository userStatusRepository;

  @Override
  public UserDto create(UserCreateRequest request, MultipartFile profile) {

    if (userRepository.findByUsername(request.username()).isPresent()) {
      throw new DuplicateUserException("이미 존재하는 유저 이름입니다.");
    }

    if (userRepository.findByEmail(request.email()).isPresent()) {
      throw new DuplicateUserException("이미 존재하는 이메일입니다.");
    }

    BinaryContent profileContent = null;
    if (profile != null && !profile.isEmpty()) {

      byte[] bytes;

      try {
        bytes = profile.getBytes();
      } catch (IOException e) {
        throw new RuntimeException("파일 읽기 실패", e);
      }

      BinaryContent binaryContent = new BinaryContent(
          profile.getOriginalFilename(),
          profile.getSize(),
          profile.getContentType(),
          bytes
      );

      binaryContentRepository.save(binaryContent);
      profileContent = binaryContent;
    }

    User user = new User(
        request.username(),
        request.email(),
        request.password(),
        profileContent
    );

    userRepository.save(user);

    UserStatus userStatus = new UserStatus(user, Instant.now());
    userStatusRepository.save(userStatus);

    return UserDto.from(user, userStatus.isOnline());
  }

  @Override
  public UserDto findById(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("유저를 찾을 수 없습니다."));

    UserStatus status = userStatusRepository.findByUserId(user.getId()).orElse(null);
    boolean isOnline = status != null && status.isOnline();

    return UserDto.from(user, isOnline);

  }

  @Override
  public List<UserDto> findAll() {
    return userRepository.findAll().stream()
        .map(user -> {
          UserStatus status = userStatusRepository.findByUserId(user.getId()).orElse(null);
          boolean isOnLine = (status != null) && status.isOnline();
          return UserDto.from(user, isOnLine);
        })
        .toList();
  }

  @Override
  public UserDto update(UUID id, UserUpdateRequest request, MultipartFile profile) {

    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));

    if (request.newUsername() != null
        && !request.newUsername().equals(user.getUsername())
        && userRepository.findByUsername(request.newUsername()).isPresent()) {
      throw new DuplicateUserException("이미 존재하는 유저 이름입니다.");
    }

    if (request.newEmail() != null
        && !request.newEmail().equals(user.getEmail())
        && userRepository.findByEmail(request.newEmail()).isPresent()) {
      throw new DuplicateUserException("이미 존재하는 이메일입니다.");
    }

    BinaryContent currentProfile = user.getProfile();
    if (profile != null && !profile.isEmpty()) {
      if (currentProfile != null) {
        binaryContentRepository.delete(currentProfile.getId());
      }

      byte[] bytes;

      try {
        bytes = profile.getBytes();
      } catch (IOException e) {
        throw new RuntimeException("파일 처리 실패", e);
      }

      BinaryContent newProfile = new BinaryContent(
          profile.getOriginalFilename(),
          profile.getSize(),
          profile.getContentType(),
          bytes
      );

      binaryContentRepository.save(newProfile);
      currentProfile = newProfile;
    }

    user.update(request.newUsername(), request.newEmail(), request.newPassword(), currentProfile);

    userRepository.save(user);

    UserStatus status = userStatusRepository.findByUserId(user.getId()).orElse(null);
    boolean isOnline = status != null && status.isOnline();

    return UserDto.from(user, isOnline);
  }

  @Override
  public void delete(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

    if (user.getProfile() != null) {
      binaryContentRepository.delete(user.getProfile().getId());
    }
    UserStatus userStatus = userStatusRepository.findByUserId(userId).orElse(null);
    if (userStatus != null) {
      userStatusRepository.delete(userStatus.getId());
    }

    userRepository.delete(userId);

  }
}
