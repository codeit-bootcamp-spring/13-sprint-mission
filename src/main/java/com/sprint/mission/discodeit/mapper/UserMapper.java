package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

  private final BinaryContentMapper binaryContentMapper;

  public UserDto toDto(User user) {

    BinaryContentDto binaryContentDto = user.getProfile() != null
        ? binaryContentMapper.toDto(user.getProfile()) : null;
    Boolean online = user.getUserStatus() != null
        ? user.getUserStatus().isOnline() : false;

    return new UserDto(
        user.getId(),
        user.getUserName(),
        user.getEmail(),
        binaryContentDto,
        online

    );

  }

}
