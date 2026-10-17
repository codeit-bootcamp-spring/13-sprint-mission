package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import org.mapstruct.*;

import java.util.*;

@Mapper(componentModel = "spring")
public interface BinaryContentMapper {

    @Mapping(target = "bytes", ignore = true)
    BinaryContentDto toDto(BinaryContent binaryContent);

    @Mapping(target = "bytes", source = "bytes")
    BinaryContentDto toDto(
            BinaryContent binaryContent,
            byte[] bytes
    );

    List<BinaryContentDto> toDtoList(
            List<BinaryContent> binaryContents
    );
}