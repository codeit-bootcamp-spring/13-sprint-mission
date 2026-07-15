package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessageMapper {

    private final BinaryContentMapper binaryContentMapper;
    private final UserMapper userMapper;

    public MessageDto toDto(Message message) {
        if (message == null) {
            return null;
        }

        List<BinaryContentDto> attachments = message.getAttachments().stream()
                .map(binaryContentMapper::toDto)
                .toList();

        return new MessageDto(
                message.getId(),
                message.getCreateAt(),
                message.getUpdateAt(),
                message.getContent(),
                message.getChannel().getId(),
                userMapper.toDto(message.getAuthor()),
                attachments
        );
    }
}