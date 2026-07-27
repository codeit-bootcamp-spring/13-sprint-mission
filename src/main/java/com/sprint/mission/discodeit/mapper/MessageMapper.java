package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import org.mapstruct.*;

import java.util.*;

@Mapper(componentModel = "spring", uses = {UserMapper.class, BinaryContentMapper.class})
public interface MessageMapper {

    MessageDto toDto(Message message);

    List<MessageDto> toDtoList(List<Message> messages);

}
