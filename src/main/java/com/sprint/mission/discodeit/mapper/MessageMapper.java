package com.sprint.mission.discodeit.mapper;


import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
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
                ,attr(message)
        );
    }

    private List<BinaryContentDto> attr(Message msg){
        if (msg.getAttachment() == null) return null;
        return msg.getAttachment().stream().map(binaryContentMapper::toDto).toList();
    }
}
