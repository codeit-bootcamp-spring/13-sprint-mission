package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {BinaryContentMapper.class, UserMapper.class})
public abstract class MessageMapper {

    @Autowired
    protected UserMapper userMapper;

    @Mapping(target = "channelId", source = "channel.id")
    @Mapping(target = "author", expression = "java(mapAuthor(message))")
    public abstract MessageDto toDto(Message message);

    protected UserDto mapAuthor(Message message) {
        if (message.getAuthor() == null) {
            return null;
        }
        return userMapper.toDto(message.getAuthor(), message.getAuthor().getStatus());
    }
}