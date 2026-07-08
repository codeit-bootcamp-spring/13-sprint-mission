package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BasicUserStatusService implements UserStatusService {

  private final UserStatusRepository userStatusRepository;
  private final UserRepository userRepository;
  // 의존성 주입

  @Transactional
  @Override
  public UserStatusDto create(UserStatusCreateRequest request) {
    // 관련된 User가 존재하지 않으면 예외 발생
    User user = userRepository.findById(request.getUserId())
        .orElseThrow(() -> new NoSuchElementException(
            "User with id " + request.getUserId() + " does not exist"));
    // 같은 User와 관련된 객체가 이미 존재하면 예외 발생
    if (userStatusRepository.existsByUserId(request.getUserId())) {
      throw new IllegalArgumentException(
          "UserStatus with id " + request.getUserId() + " already exists");
    }
    UserStatus userStatus = new UserStatus(user, request.getLastActiveAt());
    UserStatus saved = userStatusRepository.save(userStatus);
    return UserStatusDto.from(saved);
  }

  @Override
  public UserStatusDto find(UUID userStatusId) { // id로 조회
    UserStatus userStatus = userStatusRepository.findById(userStatusId)
        .orElseThrow(
            () -> new NoSuchElementException("UserStatus with id " + userStatusId + " not found"));
    return UserStatusDto.from(userStatus);
  }

  @Override
  public List<UserStatusDto> findAll() { // 모든 객체 조회
    return userStatusRepository.findAll().stream()
        .map(UserStatusDto::from)
        .toList();
  }

  @Transactional
  @Override
  public UserStatusDto update(UUID userStatusId, UserStatusUpdateRequest request) {
    UserStatus userStatus = userStatusRepository.findByUserId(userStatusId)
        .orElseThrow(
            () -> new NoSuchElementException("UserStatus with id " + userStatusId + " not found"));
    userStatus.update(request.getNewLastActiveAt());
    return UserStatusDto.from(userStatus);
  }

  @Transactional
  @Override
  public UserStatusDto updateByUserId(UUID userId,
      UserStatusUpdateRequest request) { // userId로 특정 User의 객체를 업데이트
    Instant newLastActiveAt = request.getNewLastActiveAt();
    UserStatus userStatus = userStatusRepository.findByUserId(userId)
        .orElseThrow(
            () -> new NoSuchElementException("UserStatus with userId " + userId + " not found"));
    userStatus.update(newLastActiveAt);
    return UserStatusDto.from(userStatus);
  }

  @Transactional
  @Override
  public void delete(UUID userStatusId) {
    if (!userStatusRepository.existsById(userStatusId)) {
      throw new NoSuchElementException("UserStatus with id " + userStatusId + " not found");
    }
    userStatusRepository.deleteById(userStatusId);
  }
}
