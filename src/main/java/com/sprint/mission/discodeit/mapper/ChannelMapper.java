package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChannelMapper {

  private final UserMapper userMapper;

  public ChannelDto toDto(
      Channel channel,
      List<User> participants,
      Instant lastMessageAt
  ) {
    List<UserDto> participantDtos = participants.stream()
        .map(userMapper::toDto)
        .toList();

    return new ChannelDto(
        channel.getId(),
        channel.getType(),
        channel.getName(),
        channel.getDescription(),
        participantDtos,
        lastMessageAt
    );
  }
}