package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MessageMapper {
    private final BinaryContentMapper binaryContentMapper;
    private final UserMapper userMapper;

    public MessageDto toDto(Message message) {
        return new MessageDto(
                message.getId()
                ,message.getCreatedAt()
                ,message.getUpdatedAt()
                ,message.getContent()
                ,message.getChannel().getId()
                ,userMapper.toDto(message.getAuthor())
                ,message.getAttachment().stream().map(binaryContentMapper::toDto).toList()
        );
    }
}
