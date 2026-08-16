package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChannelMapper {

  private final MessageRepository messageRepository;
  private final UserMapper userMapper;
  private final ReadStatusRepository readStatusRepository;

  public ChannelDto toDto(Channel channel) {
    Instant lastMessageAt = messageRepository.findFirstByChannel_IdOrderByCreatedAtDesc(
            channel.getId())
        .map(Message::getCreatedAt)
        .orElse(null);

    List<UserDto> userDtoList = channel.getType() == ChannelType.PRIVATE ?
        readStatusRepository.findAllByChannel_Id(channel.getId())
        .stream()
        .map(readStatus -> userMapper.toDto(readStatus.getUser()))
        .toList()
        : List.of();

    return new ChannelDto(
        channel.getId(),
        channel.getType(),
        channel.getName(),
        channel.getDescription(),
        userDtoList,
        lastMessageAt
    );
  }
}