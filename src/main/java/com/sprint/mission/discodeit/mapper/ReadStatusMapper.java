package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.entity.ReadStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReadStatusMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "channelId", source = "channel.id")
    @Mapping(target = "lastReadTime", source = "lastReadAt")
    @Mapping(
            target = "newNotificationEnabled",
            source = "notificationEnabled"
    )
    ReadStatusDto toDto(ReadStatus readStatus);

    List<ReadStatusDto> toDtoList(
            List<ReadStatus> readStatuses
    );
}