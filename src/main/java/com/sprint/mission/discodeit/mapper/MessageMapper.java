package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class, BinaryContentMapper.class})
public interface MessageMapper {

    @Mapping(target = "id", source = "message.id")
    @Mapping(target = "content", source = "message.content")
    @Mapping(target = "channelId", source = "message.channel.id")
    @Mapping(target = "author", source = "author")
    @Mapping(target = "attachments", source = "message.attachments")
    @Mapping(target = "createdAt", source = "message.createdAt")
    MessageDto toDto(Message message, UserDto author);

}
