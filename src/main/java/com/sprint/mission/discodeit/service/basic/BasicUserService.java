package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentStorageException;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicUserService implements UserService {

  private final UserRepository repository;
  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentStorage binaryContentStorage;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public UserResponse createUser(UserCreateRequest request, MultipartFile profile) {
    log.info("사용자 생성 시작: username={}, profileAttached={}",
        request.username(), profile != null && !profile.isEmpty());

    String userName = request.username();
    String email = request.email();
    String phoneNumber = request.phoneNumber();

    validateDuplicateUser(userName, email, phoneNumber);

    User user = request.toEntity();

    //유저 비밀번호 암호화
    String hashedPassword = passwordEncoder.encode(user.getPassword());
    user.updatePassword(hashedPassword);

    //프로필 생성
    if (profile != null && !profile.isEmpty()) {
      String savedFileName = profile.getOriginalFilename();
      Long size = profile.getSize();
      String contentType = profile.getContentType();

      BinaryContent binaryContent = new BinaryContent(savedFileName, size, contentType, user, null);
      user.updateProfile(binaryContent);
      log.info("프로필 파일 업로드 시작: binaryContentId={}, size={}, contentType={}",
          binaryContent.getId(), size, contentType);

      try {
        byte[] bytes = profile.getBytes();
        binaryContentStorage.put(binaryContent.getId(), bytes);
        log.info("프로필 파일 업로드 완료: binaryContentId={}", binaryContent.getId());
      } catch (IOException e) {
        throw new BinaryContentStorageException(savedFileName, e);
      }
    } else {
      log.info("유저 프로필에 첨부파일이 없습니다.");
    }

    repository.save(user);
    log.info("유저 생성 - {}", user.getUsername());
    return userMapper.toDto(user);
  }

  @Override
  @Transactional(readOnly = true)
  public UserResponse findUserById(UUID userId) {
    User user = getUserOrThrow(userId);
    log.info("유저 조회 - {}", user.getUsername());
    return userMapper.toDto(user);
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserResponse> findAllUser() {
    log.info("전체 유저 조회 : ");

    return repository.findAll().stream().map(userMapper::toDto).toList();
  }

  @Override
  @Transactional
  @PreAuthorize("#userId == principal.userDto.id")
  public UserResponse updateUser(UUID userId, UserUpdateRequest request, MultipartFile profile) {
    log.info("사용자 수정 시작: userId={}, profileAttached={}",
        userId, profile != null && !profile.isEmpty());

    User userToUpdate = getUserOrThrow(userId);

    String newUsername = request.newUsername();
    String newEmail = request.newEmail();
    String newPhoneNumber = request.phoneNumber();
    String newPassword = request.newPassword();

    validateDuplicateUserForUpdate(userId, newUsername, newEmail, newPhoneNumber);

    if (newUsername != null) {
      userToUpdate.updateUserName(newUsername);
    }
    if (newEmail != null) {
      userToUpdate.updateEmail(newEmail);
    }
    if (newPhoneNumber != null) {
      userToUpdate.updatePhoneNumber(newPhoneNumber);
    }
    if (newPassword != null) {
      userToUpdate.updatePassword(newPassword);
    }

    if (profile != null && !profile.isEmpty()) {
      String savedFileName = profile.getOriginalFilename();
      Long size = profile.getSize();
      String contentType = profile.getContentType();

      BinaryContent binaryContent = new BinaryContent(savedFileName, size, contentType,
          userToUpdate, null);
      binaryContentRepository.save(binaryContent);
      userToUpdate.updateProfile(binaryContent);
      log.info("프로필 파일 업로드 시작: binaryContentId={}, userId={}, size={}, contentType={}",
          binaryContent.getId(), userId, size, contentType);

      try {
        byte[] bytes = profile.getBytes();
        binaryContentStorage.put(binaryContent.getId(), bytes);
        log.info("프로필 파일 업로드 완료: binaryContentId={}, userId={}",
            binaryContent.getId(), userId);
      } catch (IOException e) {
        throw new BinaryContentStorageException(savedFileName, e);
      }
    } else {
      log.info("유저 프로필에 첨부파일이 없습니다.");
    }

    repository.save(userToUpdate);

    log.info("유저 수정 - {}", userToUpdate.getUsername());
    return userMapper.toDto(userToUpdate);
  }

  @Override
  @Transactional
  @PreAuthorize("#userId == principal.userDto.id")
  public void deleteUser(UUID userId) {
    log.info("사용자 삭제 시작: userId={}", userId);
    User user = getUserOrThrow(userId);
    if (user.getProfile() != null && user.getProfile().getId() != null) {
      binaryContentRepository.deleteById(user.getProfile().getId());
    }
    repository.deleteById(userId);

    log.info("사용자 삭제 완료: userId={}", userId);
  }

  @Override
  public UserResponse changeRole(UUID userid, Role role) {

    User user = getUserOrThrow(userid);
    user.updateRole(role);
    repository.save(user);

    return userMapper.toDto(user);
  }


  private User getUserOrThrow(UUID id) {
    return repository.findById(id)
        .orElseThrow(() -> new UserNotFoundException("userId", id));
  }

  private void validateDuplicateUser(String userName, String email, String phoneNumber) {
    if (email != null && repository.existsByEmail(email)) {
      throw new UserAlreadyExistsException("email", email);
    }
    // 전화번호는 선택 입력값이므로, 값이 있을 때만 중복 검사한다.
    if (phoneNumber != null && !phoneNumber.isBlank() && repository.existsByPhoneNumber(
        phoneNumber)) {
      throw new UserAlreadyExistsException("phoneNumber", phoneNumber);
    }
    if (userName != null && repository.existsByUsername(userName)) {
      throw new UserAlreadyExistsException("username", userName);
    }
  }

  private void validateDuplicateUserForUpdate(UUID currentUserId, String userName, String email,
      String phoneNumber) {
    repository.findAll().stream()
        .filter(user -> !user.getId().equals(currentUserId))
        .forEach(user -> {
          if (email != null && Objects.equals(user.getEmail(), email)) {
            throw new UserAlreadyExistsException("email", email);
          }
          if (phoneNumber != null && !phoneNumber.isBlank()
              && Objects.equals(user.getPhoneNumber(), phoneNumber)) {
            throw new UserAlreadyExistsException("phoneNumber", phoneNumber);
          }
          if (userName != null && Objects.equals(user.getUsername(), userName)) {
            throw new UserAlreadyExistsException("username", userName);
          }
        });
  }

}
