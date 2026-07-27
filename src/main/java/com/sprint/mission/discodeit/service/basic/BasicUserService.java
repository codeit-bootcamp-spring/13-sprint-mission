package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentUploadException;
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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
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
    log.debug("사용자 생성 시작: hasProfile={}", profile != null && !profile.isEmpty());

    if (userRepository.findByUsername(request.username()).isPresent()) {
      log.warn("사용자 생성 실패: 사용자 이름 중복");
      throw new DuplicateUserException("username", request.username());
    }

    if (userRepository.findByEmail(request.email()).isPresent()) {
      log.warn("사용자 생성 실패: 이메일 중복");
      throw new DuplicateUserException("email", request.email());
    }

    BinaryContent profileContent = null;
    if (profile != null && !profile.isEmpty()) {
      log.debug("프로필 파일 업로드 시작: fileName={}, size={}",
          profile.getOriginalFilename(), profile.getSize());

      byte[] bytes;

      try {
        bytes = profile.getBytes();
      } catch (IOException e) {
        log.error("프로필 파일 읽기 실패: fileName={}", profile.getOriginalFilename(), e);
        throw new BinaryContentUploadException(profile.getOriginalFilename(), e);
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

    log.info("사용자 생성 완료: userId={}", user.getId());

    return userMapper.toDto(user);
  }

  @Transactional(readOnly = true)
  @Override
  public UserDto findById(UUID userId) {
    log.debug("사용자 단건 조회 시작: userId={}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> {
          log.warn("시용자 조회 실패: userId={}", userId);
          return new UserNotFoundException(userId);
        });

    log.debug("사용자 단건 조회 완료: userId={}", userId);

    return userMapper.toDto(user);
  }

  @Transactional(readOnly = true)
  @Override
  public List<UserDto> findAll() {
    log.debug("사용자 목록 조회 시작");

    List<UserDto> users = userRepository.findAll().stream()
        .map(user -> userMapper.toDto(user))
        .toList();

    log.debug("사용자 목록 조회 완료: count={}", users.size());

    return users;
  }

  @Override
  public UserDto update(UUID id, UserUpdateRequest request, MultipartFile profile) {
    log.debug("사용자 수정 시작: userId={}, hasProfile={}", id, profile != null && !profile.isEmpty());

    User user = userRepository.findById(id)
        .orElseThrow(() -> {
          log.warn("사용자 수정 실패: userId={}를 찾을 수 없음", id);
          return new UserNotFoundException(id);
        });

    if (request.newUsername() != null
        && !request.newUsername().equals(user.getUsername())
        && userRepository.findByUsername(request.newUsername()).isPresent()) {

      log.warn("사용자 수정 실패: 사용자 이름 중복, userId={}", id);
      throw new DuplicateUserException("username", request.newUsername());
    }

    if (request.newEmail() != null
        && !request.newEmail().equals(user.getEmail())
        && userRepository.findByEmail(request.newEmail()).isPresent()) {

      log.warn("사용자 수정 실패: 이메일 중복, userId={}", id);
      throw new DuplicateUserException("email", request.newEmail());
    }

    BinaryContent currentProfile = user.getProfile();

    if (profile != null && !profile.isEmpty()) {
      if (currentProfile != null) {
        log.debug("기존 프로필 정보 삭제: binaryContentId={}", currentProfile.getId());

        binaryContentRepository.deleteById(currentProfile.getId());
      }

      byte[] bytes;

      try {
        bytes = profile.getBytes();
      } catch (IOException e) {
        log.error("프로필 파일 읽기 실패: userId={}, fileName={}", id, profile.getOriginalFilename(), e);
        throw new BinaryContentUploadException(profile.getOriginalFilename(), e);
      }

      BinaryContent newProfile = new BinaryContent(
          profile.getOriginalFilename(),
          profile.getSize(),
          profile.getContentType()
      );

      binaryContentRepository.save(newProfile);
      binaryContentStorage.put(newProfile.getId(), bytes);
      currentProfile = newProfile;

      log.debug("새 프로필 파일 업로드 완료: userId={}, binaryContentId={}", id, newProfile.getId());
    }

    user.update(request.newUsername(), request.newEmail(), request.newPassword(), currentProfile);

    log.info("사용자 수정 완료: userId={}", id);

    return userMapper.toDto(user);
  }

  @Override
  public void delete(UUID userId) {
    log.debug("사용자 삭제 시작: userId={}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> {
          log.debug("사용자 삭제 실패: userId={}를 찾을 수 없음", userId);
          return new UserNotFoundException(userId);
        });

    if (user.getProfile() != null) {
      log.debug("사용자 프로필 정보 삭제: userId={}, binaryContentId={}",
          userId, user.getProfile().getId());

      binaryContentRepository.deleteById(user.getProfile().getId());
    }

    userRepository.delete(user);

    log.info("사용자 삭제 완료: userId={}", userId);
  }
}
