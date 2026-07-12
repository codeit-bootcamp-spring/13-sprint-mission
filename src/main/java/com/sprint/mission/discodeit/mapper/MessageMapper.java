package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageMapper {

  private final BinaryContentMapper binaryContentMapper;
  private final UserMapper userMapper;

  public MessageDto toDto(Message message) {
    return MessageDto.builder()
        .id(message.getId())
        .createdAt(message.getCreatedAt())
        .updatedAt(message.getUpdatedAt())
        .channelId(message.getChannel().getId())
        .author(message.getAuthor() == null ? null
            : userMapper.toDto(message.getAuthor()))
        .content(message.getContent())
        .attachments(message.getAttachments().stream()
            .map(binaryContentMapper::toDto).toList())
        .build();
  }
}
