package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChannelMapper {

  private final MessageRepository messageRepository;
  private final ReadStatusRepository readStatusRepository;
  private final UserMapper userMapper;

  public ChannelDto toDto(Channel channel) {
    return ChannelDto.builder()
        .id(channel.getId())
        .type(channel.getType())
        .name(channel.getName())
        .description(channel.getDescription())
        .lastMessageAt(
            messageRepository.findByChannelId(channel.getId()).stream()
                .map(Message::getUpdatedAt)
                .max(Comparator.naturalOrder())
                .orElse(null))
        .participants(
            channel.getType() == ChannelType.PRIVATE ?
                readStatusRepository.findByChannelId(channel.getId()).stream()
                    .map(ReadStatus::getUser)
                    .map(userMapper::toDto)
                    .toList()
                : List.of()
        ).build();
  }
}