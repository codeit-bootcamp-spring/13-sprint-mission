package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicUserStatusService implements UserStatusService {

  private final UserStatusRepository userStatusRepository;
  private final UserRepository userRepository;
  private final UserStatusMapper userStatusMapper;
  private final UserMapper userMapper;

  @Override
  @Transactional
  public UserStatusDto create(UserStatusCreateRequest request) {
    log.info("UserStatus 생성 요청 - userId: {}", request.userId());

    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 User입니다."));
    userStatusRepository.findByUser_Id(request.userId())
        .ifPresent(userStatus -> {
          throw new IllegalArgumentException("이미 존재하는 사용자 상태입니다.");
        });
    UserStatusDto result = userStatusMapper
        .toDto(userStatusRepository
            .save(new UserStatus(user, request.lastActiveAt())));

    log.info("UserStatus 생성 완료 - userId: {}", request.userId());
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public UserStatusDto find(UUID userStatusId) {
    log.info("UserStatus 단건 조회 요청- userStatusId: {}", userStatusId);

    UserStatus userStatus = userStatusRepository.findById(userStatusId)
        .orElseThrow(
            () -> new NoSuchElementException("존재하지 않는 userStatusId입니다."));

    log.info("UserStatus 단건 조회 완료- userStatusId: {}", userStatusId);
    return userStatusMapper.toDto(userStatus);
    // 단건 조회시 UserStatus 하나를 찾아서 바로 변환
    // stream, Optional의 map 불필요.
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserStatusDto> findAll() {
    log.info("UserStatus 전체 조회 요청");

    List<UserStatusDto> result = userStatusRepository.findAll().stream()
        .map(userStatusMapper::toDto)
        .toList();
    // 다건 조회시 list에 객체를 각각 toDto메서드로 변환해야해서 stream 사용.

    log.info("UserStatus 전체 조회 완료 - 조회된 수: {}", result.size());
    return result;
  }

  @Override
  @Transactional
  public UserStatusDto update(UUID userStatusId, UserStatusUpdateRequest request) {
    log.info("UserStatus 수정 요청 - userStatusId: {}", userStatusId);

    UserStatus userStatus = userStatusRepository.findById(userStatusId)
        .orElseThrow(
            () -> new NoSuchElementException("존재하지 않는 userStatusId입니다."));
    userStatus.updateLastActiveAt(request.newLastActiveAt());

    log.info("UserStatus 수정 완료 - userStatusId: {}", userStatusId);
    return userStatusMapper.toDto(userStatus);
  }

  @Override
  @Transactional
  public UserDto updateByUserId(UUID userId, UserStatusUpdateRequest request) {
    log.info("UserStatus 수정 요청 - userId: {}", userId);

    UserStatus userStatus = userStatusRepository.findByUser_Id(userId)
        .orElseThrow(
            () -> new NoSuchElementException("존재하지 않는 userId입니다."));
    userStatus.updateLastActiveAt(request.newLastActiveAt());

    log.info("UserStatus 수정 완료 - userId: {}", userId);
    return userMapper.toDto(userStatus.getUser());
  }

  @Override
  @Transactional
  public void delete(UUID userStatusId) {
    log.info("UserStatus 삭제 요청 - userStatusId: {}", userStatusId);

    if (!userStatusRepository.existsById(userStatusId)) {
      throw new NoSuchElementException("존재하지 않는 userStatusId입니다.");
    }
    userStatusRepository.deleteById(userStatusId);
    log.info("UserStatus 삭제 완료 - userStatusId: {}", userStatusId);
  }
}