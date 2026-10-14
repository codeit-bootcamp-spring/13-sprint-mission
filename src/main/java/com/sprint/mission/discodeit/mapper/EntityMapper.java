package com.sprint.mission.discodeit.mapper;


import com.sprint.mission.discodeit.dto.projection.ChannelProjection;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.security.CertificationChecker;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,  // nll 필드 덮어씌우기 안함 (patch)
        nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_NULL,    // list 가 비어있으면 null 반환.
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, // null 필드 생성 안함 (put)
        imports = {
                CertificationChecker.class
        }
)
public interface EntityMapper {


    @Mapping(source = "projection.id", target = "id")
    @Mapping(source = "projection.type", target = "type")
    @Mapping(source = "projection.name", target = "name")
    @Mapping(source = "projection.description", target = "description")
    @Mapping(source = "users", target = "participants")
    @Mapping(source = "projection.lastMessageAt", target = "lastMessageAt")
    ChannelDto toDto(ChannelProjection projection, List<UserDto> users);


    @Mapping(source = "user.id",target = "userId")
    @Mapping(source = "channel.id",target = "channelId")
    ReadStatusDto toDto(ReadStatus readStatus);

    BinaryContentDto toDto(BinaryContent binaryContent);

    @Mapping(source = "online", target = "online")
    UserDto toDto(User user, boolean online);

    @Mapping(source = "channel.id", target = "channelId")
    MessageDto toDto(Message message);

    NotificationDto toDto(Notification noty);
}
