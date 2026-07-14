package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {BinaryContentMapper.class, UserMapper.class})
public interface MessageMapper {

    @Mapping(target = "channelId", source = "channel.id")
    @Mapping(target = "author", expression = "java(mapAuthor(message))")
    MessageDto toDto(Message message);

    default UserDto mapAuthor(Message message) {
        if (message.getAuthor() == null) {
            return null;
        }
        return Mappers.getMapper(UserMapper.class)
                .toDto(message.getAuthor(), message.getAuthor().getStatus());
    }
}
