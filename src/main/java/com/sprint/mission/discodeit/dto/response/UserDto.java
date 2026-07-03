package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.User;
import java.util.UUID;

public record UserDto(
    UUID id,
    String username,
    String email,
    BinaryContentDto profileId,
    Boolean online
) {

  public static UserDto from(User user, boolean online) {
    return new UserDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        null,
        false
    );
  }

}
