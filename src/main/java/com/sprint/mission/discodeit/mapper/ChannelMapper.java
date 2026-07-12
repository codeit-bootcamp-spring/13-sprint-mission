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
  //ChannelMapper는 repository를 직접 주입받음.
  //MapStruct는 repository를 주입 못함.
  // -> 복잡한 로직의 경우 하드코딩도 방법일 수 있음!

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
        : null;
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