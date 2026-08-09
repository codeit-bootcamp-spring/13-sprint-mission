package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userstatus.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusDto;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusNotFoundException;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class BasicUserStatusService implements UserStatusService {

  private final UserStatusRepository userStatusRepository;
  private final UserRepository userRepository;
  private final UserStatusMapper userStatusMapper;
  // 의존성 주입

  @Transactional
  @Override
  public UserStatusDto create(UserStatusCreateRequest request) {
    // 관련된 User가 존재하지 않으면 예외 발생
    User user = userRepository.findById(request.getUserId())
        .orElseThrow(() -> new UserNotFoundException(request.getUserId()));
    // 같은 User와 관련된 객체가 이미 존재하면 예외 발생
    if (userStatusRepository.existsByUserId(request.getUserId())) {
      throw new UserStatusAlreadyExistsException(request.getUserId());
    }
    UserStatus userStatus = new UserStatus(user, request.getLastActiveAt());
    UserStatus saved = userStatusRepository.save(userStatus);
    return userStatusMapper.toDto(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public UserStatusDto find(UUID userStatusId) { // id로 조회
    return userStatusRepository.findById(userStatusId)
        .map(status -> userStatusMapper.toDto(status))
        .orElseThrow(
            () -> new UserStatusNotFoundException("조회 시도한 사용자 상태의 ID 정보", userStatusId));
  }

  @Transactional(readOnly = true)
  @Override
  public List<UserStatusDto> findAll() { // 모든 객체 조회
    return userStatusRepository.findAll().stream()
        .map(userStatusMapper::toDto)
        .toList();
  }

  @Transactional
  @Override
  public UserStatusDto update(UUID userStatusId, UserStatusUpdateRequest request) {
    UserStatus userStatus = userStatusRepository.findByUserId(userStatusId)
        .orElseThrow(
            () -> new UserStatusNotFoundException("조회 시도한 사용자 상태의 ID 정보", userStatusId));
    userStatus.update(request.getNewLastActiveAt());
    return userStatusMapper.toDto(userStatus);
  }

  @Transactional
  @Override
  public UserStatusDto updateByUserId(UUID userId,
      UserStatusUpdateRequest request) { // userId로 특정 User의 객체를 업데이트
    UserStatus userStatus = userStatusRepository.findByUserId(userId)
        .orElseThrow(
            () -> new UserStatusNotFoundException("조회 시도한 사용자 상태의 사용자 ID 정보", userId));
    userStatus.update(request.getNewLastActiveAt());
    return userStatusMapper.toDto(userStatus);
  }

  @Transactional
  @Override
  public void delete(UUID userStatusId) {
    if (!userStatusRepository.existsById(userStatusId)) {
      throw new UserStatusNotFoundException("조회 시도한 사용자 상태의 ID 정보", userStatusId);
    }
    userStatusRepository.deleteById(userStatusId);
  }
}
