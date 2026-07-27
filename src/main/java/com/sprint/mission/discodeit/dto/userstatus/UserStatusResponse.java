package com.sprint.mission.discodeit.dto.userstatus;

import com.sprint.mission.discodeit.entity.UserStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class UserStatusResponse {

  private UUID userId;
  private Instant lastActiveAt;

  public static UserStatusResponse from(UserStatus userStatus) {
    return UserStatusResponse.builder()
        .userId(userStatus.getUser().getId())
        .lastActiveAt(userStatus.getLastActiveAt())
        .build();
  }
}
