package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.userstatus.UserStatusNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.time.Instant;
import java.util.List;
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
        .orElseThrow(() -> new UserNotFoundException(request.userId()));

    userStatusRepository.findByUser_Id(request.userId())
        .ifPresent(userStatus -> {
          throw new UserStatusAlreadyExistsException(request.userId());
        });

    UserStatusDto result = userStatusMapper
        .toDto(userStatusRepository
            .save(new UserStatus(user, Instant.now())));

    log.info("UserStatus 생성 완료 - userId: {}", request.userId());
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public UserStatusDto find(UUID userStatusId) {
    log.info("UserStatus 단건 조회 요청- userStatusId: {}", userStatusId);

    UserStatus userStatus = userStatusRepository.findById(userStatusId)
        .orElseThrow(
            () -> new UserStatusNotFoundException(userStatusId));

    log.info("UserStatus 단건 조회 완료- userStatusId: {}", userStatusId);
    return userStatusMapper.toDto(userStatus);
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserStatusDto> findAll() {
    log.info("UserStatus 전체 조회 요청");

    List<UserStatusDto> result = userStatusRepository.findAll().stream()
        .map(userStatusMapper::toDto)
        .toList();

    log.info("UserStatus 전체 조회 완료 - 조회된 수: {}", result.size());
    return result;
  }

  @Override
  @Transactional
  public UserStatusDto updateToNow(UUID userStatusId) {
    log.info("UserStatus 수정 요청 - userStatusId: {}", userStatusId);

    UserStatus userStatus = userStatusRepository.findById(userStatusId)
        .orElseThrow(
            () -> new UserStatusNotFoundException(userStatusId));
    userStatus.updateLastActiveAt(Instant.now());

    log.info("UserStatus 수정 완료 - userStatusId: {}", userStatusId);
    return userStatusMapper.toDto(userStatus);
  }

  @Override
  @Transactional
  public UserDto updateToNowByUserId(UUID userId) {
    log.info("UserStatus 수정 요청 - userId: {}", userId);

    UserStatus userStatus = userStatusRepository.findByUser_Id(userId)
        .orElseThrow(
            () -> new UserNotFoundException(userId));
    userStatus.updateLastActiveAt(Instant.now());

    log.info("UserStatus 수정 완료 - userId: {}", userId);
    return userMapper.toDto(userStatus.getUser());
  }

  @Override
  @Transactional
  public void delete(UUID userStatusId) {
    log.info("UserStatus 삭제 요청 - userStatusId: {}", userStatusId);

    if (!userStatusRepository.existsById(userStatusId)) {
      throw new UserStatusNotFoundException(userStatusId);
    }
    userStatusRepository.deleteById(userStatusId);
    log.info("UserStatus 삭제 완료 - userStatusId: {}", userStatusId);
  }
}