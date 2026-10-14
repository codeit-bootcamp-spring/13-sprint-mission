package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.exception.user.DuplicateUserException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.JwtRegistry;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BasicUserService implements UserService {

  private final UserRepository userRepository;
  private final BinaryContentService binaryContentService;
  private final BinaryContentRepository binaryContentRepository;
  private final UserMapper userMapper;
  private final BinaryContentStorage binaryContentStorage;
  private final PasswordEncoder passwordEncoder;
  private final JwtRegistry jwtRegistry;
  private final ApplicationEventPublisher eventPublisher;


  @Override
  @CacheEvict(cacheNames = "users", allEntries = true)
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
      log.debug(
          "프로필 파일 업로드 시작: fileName={}, size={}",
          profile.getOriginalFilename(),
          profile.getSize()
      );

      profileContent = binaryContentService.create(profile);
    }

    String encodedPassword = passwordEncoder.encode(request.password());

    User user = new User(
        request.username(),
        request.email(),
        encodedPassword,
        profileContent,
        Role.USER
    );

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
  @Cacheable(cacheNames = "users")
  public List<UserDto> findAll() {
    log.debug("사용자 목록 조회 시작");

    List<UserDto> users = userRepository.findAll().stream()
        .map(user -> userMapper.toDto(user))
        .toList();

    log.debug("사용자 목록 조회 완료: count={}", users.size());

    return users;
  }

  @Override
  @Caching(evict = {
      @CacheEvict(cacheNames = "users", allEntries = true),
      @CacheEvict(cacheNames = "channels", allEntries = true)
  })
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
        UUID currentProfileId = currentProfile.getId();

        log.debug(
            "기존 프로필 정보 삭제: binaryContentId={}",
            currentProfileId
        );

        binaryContentStorage.delete(currentProfileId);
        binaryContentRepository.deleteById(currentProfileId);
      }

      currentProfile = binaryContentService.create(profile);

      log.debug(
          "새 프로필 파일 업로드 요청 완료: userId={}, binaryContentId={}",
          id,
          currentProfile.getId()
      );
    }

    String encodedPassword =
        request.newPassword() != null
            ? passwordEncoder.encode(request.newPassword())
            : null;

    user.update(
        request.newUsername(),
        request.newEmail(),
        encodedPassword,
        currentProfile
    );

    log.info("사용자 수정 완료: userId={}", id);

    return userMapper.toDto(user);
  }

  @Override
  @Caching(evict = {
      @CacheEvict(cacheNames = "users", allEntries = true),
      @CacheEvict(cacheNames = "channels", allEntries = true)
  })
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

      UUID profileId = user.getProfile().getId();

      binaryContentStorage.delete(profileId);
      binaryContentRepository.deleteById(profileId);
    }

    userRepository.delete(user);

    log.info("사용자 삭제 완료: userId={}", userId);
  }

  @Override
  @Caching(evict = {
      @CacheEvict(cacheNames = "users", allEntries = true),
      @CacheEvict(cacheNames = "channels", allEntries = true)
  })
  public UserDto updateRole(UserRoleUpdateRequest request) {

    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new UserNotFoundException(request.userId()));

    Role oldRole = user.getRole();
    Role newRole = request.newRole();

    user.updateRole(newRole);

    jwtRegistry.invalidateJwtInformationByUserId(request.userId());

    if (oldRole != newRole) {
      eventPublisher.publishEvent(
          new RoleUpdatedEvent(user.getId(), oldRole, newRole)
      );
    }

    return userMapper.toDto(user);
  }
}
