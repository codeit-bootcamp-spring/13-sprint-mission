package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.entity.ReadStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReadStatusMapper {

  @Mapping(target = "userId", source = "user.id")
  //target = DTO필드/ source = user Entity의 필드(.id로 getId()호출.)
  // 즉, user Entity의 id필드를 Dto의 userId로 맵핑함.
  @Mapping(target = "channelId", source = "channel.id")
  ReadStatusDto toDto(ReadStatus readStatus);

}
