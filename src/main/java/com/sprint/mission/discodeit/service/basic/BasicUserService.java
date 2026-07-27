package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.DuplicateUserException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final UserMapper userMapper;
  private final BinaryContentStorage binaryContentStorage;


  @Override
  public UserDto create(UserCreateRequest request, MultipartFile profile) {

    if (userRepository.findByUsername(request.username()).isPresent()) {
      throw new DuplicateUserException("username", request.username());
    }

    if (userRepository.findByEmail(request.email()).isPresent()) {
      throw new DuplicateUserException("email", request.email());
    }

    BinaryContent profileContent = null;
    if (profile != null && !profile.isEmpty()) {

      byte[] bytes;

      try {
        bytes = profile.getBytes();
      } catch (IOException e) {
        throw new RuntimeException("파일 읽기 실패", e);
      }

      BinaryContent content = new BinaryContent(
          profile.getOriginalFilename(),
          profile.getSize(),
          profile.getContentType()
      );

      binaryContentRepository.save(content);
      binaryContentStorage.put(content.getId(), bytes);
      profileContent = content;
    }

    User user = new User(
        request.username(),
        request.email(),
        request.password(),
        profileContent
    );

    UserStatus userStatus = new UserStatus(user, Instant.now());
    user.updateStatus(userStatus);

    userRepository.save(user);

    return userMapper.toDto(user);
  }

  @Transactional(readOnly = true)
  @Override
  public UserDto findById(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("유저를 찾을 수 없습니다."));

    return userMapper.toDto(user);
  }

  @Transactional(readOnly = true)
  @Override
  public List<UserDto> findAll() {
    return userRepository.findAll().stream()
        .map(user -> userMapper.toDto(user))
        .toList();
  }

  @Override
  public UserDto update(UUID id, UserUpdateRequest request, MultipartFile profile) {

    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));

    if (request.newUsername() != null
        && !request.newUsername().equals(user.getUsername())
        && userRepository.findByUsername(request.newUsername()).isPresent()) {
      throw new DuplicateUserException("username", request.newUsername());
    }

    if (request.newEmail() != null
        && !request.newEmail().equals(user.getEmail())
        && userRepository.findByEmail(request.newEmail()).isPresent()) {
      throw new DuplicateUserException("email", request.newEmail());
    }

    BinaryContent currentProfile = user.getProfile();
    if (profile != null && !profile.isEmpty()) {
      if (currentProfile != null) {
        binaryContentRepository.deleteById(currentProfile.getId());
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
          profile.getContentType()
      );

      binaryContentRepository.save(newProfile);
      binaryContentStorage.put(newProfile.getId(), bytes);
      currentProfile = newProfile;
    }

    user.update(request.newUsername(), request.newEmail(), request.newPassword(), currentProfile);

    return userMapper.toDto(user);
  }

  @Override
  public void delete(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));

    if (user.getProfile() != null) {
      binaryContentRepository.deleteById(user.getProfile().getId());
    }

    userRepository.delete(user);
  }
}
