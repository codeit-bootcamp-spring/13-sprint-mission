package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.FileUploadDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  private final BinaryContentStorage binaryContentStorage;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
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
      try {
        binaryContentStorage.put(user.getProfile().getId(), profile.bytes());
      } catch (Exception e) {
        log.error("User 프로필 이미지 저장 중 서버 오류 발생 - userId: {}", user.getId(), e);
        throw new RuntimeException("프로필 이미지 저장 중 오류 발생", e);
      }
    }

    UserStatus userStatus = new UserStatus(user);
    userStatusRepository.save(userStatus);

    log.info("User 생성 완료 - userId: {}", user.getId());
    return userMapper.toDto(user);
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
  public List<UserDto> findAllUsers() {
    return findAll();
  }

  @Override
  @Transactional
  public UserDto update(UUID id, String newEmail, String newUsername, String newPassword,
      String statusMessage, FileUploadDto profile) {
    log.debug("User 수정 요청 - userId: {}", id);
    User user = userRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("User 수정 실패 - 존재하지 않는 userId: {}", id);
          return new UserNotFoundException(Map.of("userId", id));
        });

    user.update(newEmail, newUsername, newPassword, statusMessage);

    if (profile != null && profile.bytes() != null && profile.bytes().length > 0) {
      saveProfileImage(user, profile);
      userRepository.saveAndFlush(user);
      try {
        binaryContentStorage.put(user.getProfile().getId(), profile.bytes());
      } catch (Exception e) {
        log.error("User 프로필 이미지 업데이트 중 서버 오류 발생 - userId: {}", id, e);
        throw new RuntimeException("프로필 이미지 업데이트 중 오류 발생", e);
      }
    }

    log.info("User 수정 완료 - userId: {}", id);
    return userMapper.toDto(user);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    log.debug("User 삭제 요청 - userId: {}", id);
    User user = userRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("User 삭제 실패 - 존재하지 않는 userId: {}", id);
          return new UserNotFoundException(Map.of("userId", id));
        });

    userStatusRepository.deleteByUserId(user.getId());
    userRepository.delete(user);
    log.info("User 삭제 완료 - userId: {}", id);
  }

  @Override
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  public UserDto updateRole(UUID userId, Role newRole) {
    log.debug("User 권한 수정 요청 - userId: {}, role: {}", userId, newRole);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException(
            Map.of("userId", userId)
        ));

    user.updateRole(newRole);

    log.info("User 권한 수정 완료 - userId: {}, role: {}", userId, newRole);

    return userMapper.toDto(user);
  }

  private void saveProfileImage(User user, FileUploadDto profile) {
    if (profile != null && profile.bytes() != null && profile.bytes().length > 0) {
      String fileName = profile.fileName();
      long fileSize = profile.size();
      String contentType = profile.contentType();
      String fileUrl = "/api/binaryContents/";

      BinaryContent profileImage = new BinaryContent(fileName, fileUrl, fileSize, contentType);
      user.updateProfile(profileImage);
    }
  }
}