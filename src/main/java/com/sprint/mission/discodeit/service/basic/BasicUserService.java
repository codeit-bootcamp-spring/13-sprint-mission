package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;
  private final BinaryContentRepository binaryContentRepository;

  @Override
  public UserDto create(String username, String email, String password, UUID profileId) {
    if (userRepository.findAll().stream().anyMatch(u -> u.getUserName().equals(username))) {
      throw new IllegalArgumentException("이미 사용중인 username입니다: " + username);
    }
    if (userRepository.findAll().stream().anyMatch(u -> u.getEmail().equals(email))) {
      throw new IllegalArgumentException("이미 사용중인 email입니다: " + email);
    }
    User user = new User(username, password, email);
    if (profileId != null) {
      user.updateProfileId(profileId);
    }
    userRepository.save(user);
    UserStatus userStatus = userStatusRepository.save(new UserStatus(user.getId(), Instant.now()));
    return new UserDto(user.getId(), user.getCreatedAt(),
        user.getUpdatedAt(), user.getUserName(), user.getEmail(),
        user.getProfileId(), userStatus.isOnline());
  }

  @Override
  public UserDto find(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));
    boolean online = userStatusRepository.findByUserId(userId)
        .map(s -> s.getLastActiveAt().isAfter(Instant.now().minusSeconds(300)))
        .orElse(false);
    return new UserDto(user.getId(), user.getCreatedAt(), user.getUpdatedAt(), user.getUserName(),
        user.getEmail(),
        user.getProfileId(), online);
  }

  @Override
  public List<UserDto> findAll() {
    return userRepository.findAll().stream()
        .map(user -> {
          boolean online = userStatusRepository.findByUserId(user.getId())
              .map(s -> s.getLastActiveAt().isAfter(Instant.now().minusSeconds(300)))
              .orElse(false);
          return new UserDto(user.getId(), user.getCreatedAt(), user.getUpdatedAt(),
              user.getUserName(), user.getEmail(),
              user.getProfileId(), online);
        })
        .toList();
  }

  @Override
  public UserDto update(UUID userId, String newUsername, String newEmail, String newPassword,
      UUID newProfileId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));
    user.update(newUsername, newEmail, newPassword);
    if (newProfileId != null) {
      user.updateProfileId(newProfileId);
    }
    userRepository.save(user);
    boolean online = userStatusRepository.findByUserId(userId)
        .map(UserStatus::isOnline)
        .orElse(false);

    return new UserDto(user.getId(), user.getCreatedAt(),
        user.getUpdatedAt(), user.getUserName(), user.getEmail(),
        user.getProfileId(), online);
  }

  @Override
  public void delete(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found"));
    if (user.getProfileId() != null) {
      binaryContentRepository.deleteById(user.getProfileId());
    }
    userStatusRepository.findByUserId(userId)
        .ifPresent(s -> userStatusRepository.deleteById(s.getId()));
    userRepository.deleteById(userId);
  }
}