package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.UserStatusDto;
import com.sprint.mission.discodeit.entity.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserStatusMapper {

  public UserStatusDto toDto(UserStatus userStatus) {
    if (userStatus == null) {
      return null;
    }

    return new UserStatusDto(
        userStatus.getId(),
        userStatus.getUser().getId(),
        userStatus.getLastActiveAt()
    );
  }

}
