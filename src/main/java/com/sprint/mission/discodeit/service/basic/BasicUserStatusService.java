package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusResponse;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.UserStatusNotFoundException;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserStatusService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;


import java.util.UUID;


@Service
@Primary
public class BasicUserStatusService implements UserStatusService {

  private final UserStatusRepository userStatusRepository;

  public BasicUserStatusService(UserStatusRepository userStatusRepository) {
    this.userStatusRepository = userStatusRepository;
  }


  @Override
  public UserStatusResponse updateByUserId(UUID userId, UserStatusUpdateRequest request) {
    UserStatus userStatus = userStatusRepository.findByUserId(userId)
        .orElseThrow(() -> new UserStatusNotFoundException(userId));

    userStatus.updateOnlineStatus(request.newLastActiveAt());

    userStatusRepository.save(userStatus);

    return convertToResponse(userStatus);
  }


  private UserStatusResponse convertToResponse(UserStatus userStatus) {

    return new UserStatusResponse(
        userStatus.getId(),
        userStatus.getCreatedAt(),
        userStatus.getUpdatedAt(),
        userStatus.getUser().getId(),
        userStatus.getLastActiveAt(),
        userStatus.isOnline()
    );
  }
}
