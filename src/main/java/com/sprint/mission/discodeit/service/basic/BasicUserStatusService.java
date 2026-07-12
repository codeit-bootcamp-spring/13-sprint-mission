package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.UserStatusNotFoundException;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicUserStatusService implements UserStatusService {

  private final UserStatusRepository userStatusRepository;
  private final UserStatusMapper userStatusMapper;


  @Override
  public UserStatusDto updateByUserId(UUID userId, UserStatusUpdateRequest request) {
    UserStatus userStatus = userStatusRepository.findByUser_Id(userId)
        .orElseThrow(() -> new UserStatusNotFoundException(userId));

    userStatus.updateOnlineStatus(request.newLastActiveAt());

    return userStatusMapper.toDto(userStatus);
  }

  @Transactional(readOnly = true)
  @Override
  public UserStatusDto findByUserId(UUID userId) {
    UserStatus userStatus = userStatusRepository.findByUser_Id(userId)
        .orElseThrow(() -> new UserStatusNotFoundException(userId));

    return userStatusMapper.toDto(userStatus);
  }
}
