package com.sprint.mission.discodeit.dto.userstatus;

import com.sprint.mission.discodeit.entity.UserStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class UserStatusDto {

  private UUID id;
  private UUID userId;
  private Instant lastActiveAt;

  public static UserStatusDto from(UserStatus userStatus) {
    return UserStatusDto.builder()
        .id(userStatus.getId())
        .userId(userStatus.getUser().getId())
        .lastActiveAt(userStatus.getLastActiveAt())
        .build();
  }
}
