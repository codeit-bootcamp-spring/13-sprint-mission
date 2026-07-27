package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.user.UserStatusNotFoundException;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BasicUserStatusService implements UserStatusService {

  private final UserStatusRepository userStatusRepository;
  private final UserStatusMapper userStatusMapper;


  @Override
  public UserStatusDto updateByUserId(
      UUID userId,
      UserStatusUpdateRequest request
  ) {
    log.debug("사용자 상태 수정 시작: userId={}", userId);

    UserStatus userStatus = userStatusRepository.findByUser_Id(userId)
        .orElseThrow(() -> {
          log.warn(
              "사용자 상태 수정 실패: userId={}의 상태를 찾을 수 없음",
              userId
          );
          return new UserStatusNotFoundException(userId);
        });

    userStatus.updateOnlineStatus(request.newLastActiveAt());

    log.info("사용자 상태 수정 완료: userId={}", userId);

    return userStatusMapper.toDto(userStatus);
  }

  @Transactional(readOnly = true)
  @Override
  public UserStatusDto findByUserId(UUID userId) {
    log.debug("사용자 상태 조회 시작: userId={}", userId);

    UserStatus userStatus = userStatusRepository.findByUser_Id(userId)
        .orElseThrow(() -> {
          log.warn(
              "사용자 상태 조회 실패: userId={}의 상태를 찾을 수 없음",
              userId
          );
          return new UserStatusNotFoundException(userId);
        });

    log.debug("사용자 상태 조회 완료: userId={}", userId);

    return userStatusMapper.toDto(userStatus);
  }
}
