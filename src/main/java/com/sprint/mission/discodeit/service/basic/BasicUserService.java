package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
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
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class BasicUserService implements UserService {

  private final BinaryContentStorage binaryContentStorage;
  private final UserMapper userMapper;
  private final UserRepository userRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final UserStatusRepository userStatusRepository;

  @Override
  public User create(UserCreateRequest userCreateRequest,
                     Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
    String username = userCreateRequest.username();
    String email = userCreateRequest.email();

    if (userRepository.existsByEmail(email)) {
      throw new IllegalArgumentException("User with email " + email + " already exists");
    }
    if (userRepository.existsByUsername(username)) {
      throw new IllegalArgumentException("User with username " + username + " already exists");
    }

    // UUID 대신 BinaryContent 객체를 직접 생성해서 전달
    BinaryContent nullableProfile = optionalProfileCreateRequest
            .map(profileRequest -> {
              BinaryContent binaryContent = new BinaryContent(
                      profileRequest.fileName(),
                      (long) profileRequest.bytes().length,
                      profileRequest.contentType()
                      // bytes 제거 — 생성자에서 빠졌기 때문
              );
              BinaryContent savedProfile = binaryContentRepository.save(binaryContent);
              binaryContentStorage.put(savedProfile.getId(), profileRequest.bytes()); // 디스크에 저장
              return savedProfile;
            })
            .orElse(null);

    User user = new User(username, email, userCreateRequest.password(), nullableProfile);
    User createdUser = userRepository.save(user);

    // UserStatus 생성 시 UUID 대신 User 객체 전달
    UserStatus userStatus = new UserStatus(createdUser, Instant.now());
    userStatusRepository.save(userStatus);

    return createdUser;
  }

  @Transactional(readOnly = true)
  @Override
  public UserDto find(UUID userId) {
    return userRepository.findById(userId)
            .map(userMapper::toDto)   // toDto() → userMapper.toDto()
            .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));
  }

  @Transactional(readOnly = true)
  @Override
  public List<UserDto> findAll() {
    return userRepository.findAll()
            .stream()
            .map(userMapper::toDto)   // toDto() → userMapper.toDto()
            .toList();
  }

  @Override
  public User update(UUID userId, UserUpdateRequest userUpdateRequest,
                     Optional<BinaryContentCreateRequest> optionalProfileCreateRequest) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));

    String newUsername = userUpdateRequest.newUsername();
    String newEmail = userUpdateRequest.newEmail();
    if (userRepository.existsByEmail(newEmail)) {
      throw new IllegalArgumentException("User with email " + newEmail + " already exists");
    }
    if (userRepository.existsByUsername(newUsername)) {
      throw new IllegalArgumentException("User with username " + newUsername + " already exists");
    }

    BinaryContent nullableProfile = optionalProfileCreateRequest
            .map(profileRequest -> {
              BinaryContent binaryContent = new BinaryContent(
                      profileRequest.fileName(),
                      (long) profileRequest.bytes().length,
                      profileRequest.contentType()
                      // bytes 제거
              );
              BinaryContent savedProfile = binaryContentRepository.save(binaryContent);
              binaryContentStorage.put(savedProfile.getId(), profileRequest.bytes()); // 디스크에 저장
              return savedProfile;
            })
            .orElse(null);

    user.update(newUsername, newEmail, userUpdateRequest.newPassword(), nullableProfile);
    return userRepository.save(user);
  }

  @Override
  public void delete(UUID userId) {
    if (!userRepository.existsById(userId)) {
      throw new NoSuchElementException("User with id " + userId + " not found");
    }
    // cascade로 BinaryContent(profile), UserStatus 자동 삭제
    userRepository.deleteById(userId);
  }



}