package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import org.mapstruct.*;

import java.util.*;

@Mapper(componentModel = "spring")
public interface ReadStatusMapper {
    ReadStatusDto toDto(ReadStatus readStatus);

    List<ReadStatusDto> toDtoList(List<ReadStatus> readStatuses);
}
