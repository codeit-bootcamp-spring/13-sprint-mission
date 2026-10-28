package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.FileUploadDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.UserChangedEvent;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final UserStatusRepository userStatusRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  @CacheEvict(cacheNames = "users", allEntries = true)
  public UserDto create(String email, String username, String password, FileUploadDto profile) {
    log.debug("User 생성 요청 - email: {}, username: {}", email, username);

    if (userRepository.existsByUsername(username)) {
      log.warn("User 생성 실패 - 이미 사용 중인 username: {}", username);
      throw new UserAlreadyExistsException(ErrorCode.DUPLICATE_USERNAME,
          Map.of("username", username));
    }
    if (userRepository.existsByEmail(email)) {
      log.warn("User 생성 실패 - 이미 가입된 email: {}", email);
      throw new UserAlreadyExistsException(ErrorCode.DUPLICATE_EMAIL, Map.of("email", email));
    }

    String encodedPassword = passwordEncoder.encode(password);

    User user = new User(email, username, encodedPassword);
    saveProfileImage(user, profile);
    userRepository.save(user);

    if (profile != null && profile.bytes() != null && profile.bytes().length > 0) {
      eventPublisher.publishEvent(
          new BinaryContentCreatedEvent(user.getProfile().getId(), profile.bytes())
      );
    }

    UserStatus userStatus = new UserStatus(user);
    userStatusRepository.save(userStatus);

    log.info("User 생성 완료 - userId: {}", user.getId());
    return publishUserEvent("users.created", user);
  }

  @Override
  public UserDto find(UUID id) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(Map.of("userId", id)));
    return userMapper.toDto(user);
  }

  @Override
  public List<UserDto> findAll() {
    return userRepository.findAll().stream()
        .map(userMapper::toDto)
        .toList();
  }

  @Override
  @Cacheable(cacheNames = "users")
  public List<UserDto> findAllUsers() {
    return findAll();
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = {"users", "channelsByUser"}, allEntries = true)
  public UserDto update(UUID id, String newEmail, String newUsername, String newPassword,
      String statusMessage, FileUploadDto profile) {
    log.debug("User 수정 요청 - userId: {}", id);
    User user = userRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("User 수정 실패 - 존재하지 않는 userId: {}", id);
          return new UserNotFoundException(Map.of("userId", id));
        });

    String encodedPassword = newPassword == null
        ? user.getPassword()
        : passwordEncoder.encode(newPassword);

    user.update(newEmail, newUsername, encodedPassword, statusMessage);

    if (profile != null && profile.bytes() != null && profile.bytes().length > 0) {
      saveProfileImage(user, profile);
      userRepository.saveAndFlush(user);

      eventPublisher.publishEvent(
          new BinaryContentCreatedEvent(user.getProfile().getId(), profile.bytes())
      );
    }

    log.info("User 수정 완료 - userId: {}", id);
    return publishUserEvent("users.updated", user);
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = {"users", "channelsByUser"}, allEntries = true)
  public void delete(UUID id) {
    log.debug("User 삭제 요청 - userId: {}", id);
    User user = userRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("User 삭제 실패 - 존재하지 않는 userId: {}", id);
          return new UserNotFoundException(Map.of("userId", id));
        });

    UserDto userDto = userMapper.toDto(user);

    userStatusRepository.deleteByUserId(user.getId());
    userRepository.delete(user);

    eventPublisher.publishEvent(
        new UserChangedEvent("users.deleted", userDto)
    );

    log.info("User 삭제 완료 - userId: {}", id);
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = {"users", "channelsByUser"}, allEntries = true)
  @PreAuthorize("hasRole('ADMIN')")
  public UserDto updateRole(UUID userId, Role newRole) {
    log.debug("User 권한 수정 요청 - userId: {}, role: {}", userId, newRole);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(
            Map.of("userId", userId)
        ));

    Role previousRole = user.getRole();

    if (previousRole != newRole) {
      user.updateRole(newRole);

      eventPublisher.publishEvent(
          new RoleUpdatedEvent(userId, previousRole, newRole)
      );
    }

    log.info("User 권한 수정 완료 - userId: {}, role: {}", userId, newRole);

    return publishUserEvent("users.updated", user);
  }

  private void saveProfileImage(User user, FileUploadDto profile) {
    if (profile != null && profile.bytes() != null && profile.bytes().length > 0) {
      String fileName = profile.fileName();
      long fileSize = profile.size();
      String contentType = profile.contentType();
      String fileUrl = "/api/binaryContents/";

      BinaryContent profileImage = new BinaryContent(
          fileName, fileUrl, fileSize, contentType
      );

      user.updateProfile(profileImage);
    }
  }

  private UserDto publishUserEvent(String eventName, User user) {
    UserDto userDto = userMapper.toDto(user);

    eventPublisher.publishEvent(
        new UserChangedEvent(eventName, userDto)
    );

    return userDto;
  }
}