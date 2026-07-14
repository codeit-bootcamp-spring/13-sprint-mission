package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.readstatus.ReadStatusDto;
import com.sprint.mission.discodeit.entity.ReadStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReadStatusMapper {

    @Mapping(target = "userId", source = "readStatus.user.id")
    @Mapping(target = "channelId", source = "readStatus.channel.id")
    ReadStatusDto toDto(ReadStatus readStatus);

}