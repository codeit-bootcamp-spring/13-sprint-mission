package com.sprint.mission.discodeit.mapper;


import com.sprint.mission.discodeit.dto.projection.ChannelProjection;
import com.sprint.mission.discodeit.dto.projection.MessageProjection;
import com.sprint.mission.discodeit.dto.projection.UserProjection;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import org.mapstruct.*;

import java.time.Instant;
import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,  // nll 필드 덮어씌우기 안함 (patch)
        nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_NULL,    // list 가 비어있으면 null 반환.
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS  // null 필드 생성 안함 (put)
//        imports = {
//                MapperMethod.class
//        }

)
public interface MapStructMapper {

    /*
    userDto 매핑 매서드
     */
    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "profile", target="profile")
    @Mapping(source = "online", target = "online")
    UserDto toDto(User user, BinaryContentDto profile, boolean online);

    @Mapping(source = "projection.id", target = "id")
    @Mapping(source = "profile", target = "profile")
    @Mapping(source = "online", target = "online")
    UserDto toDto(UserProjection projection, BinaryContentDto profile, boolean online);


    /*
    channelDto 매핑 매서드
     */
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




    @Mapping(source = "message.id",target = "id")
    @Mapping(source = "message.channel.id",target = "channelId")
    @Mapping(source = "author",target = "author")
    @Mapping(source = "attachments",target = "attachments")
    MessageDto toDto(Message message, UserDto author, List<BinaryContentDto> attachments);

    @Mapping(source = "projection.id",target = "id")
    @Mapping(source = "projection.channelId",target = "channelId")
    @Mapping(source = "author",target = "author")
    @Mapping(source = "attachments",target = "attachments")
    MessageDto toDto(MessageProjection projection, UserDto author, List<BinaryContentDto> attachments);



    // todo - 매퍼 매서드 삭제 예정.
    @Mapping(source = "userDtoList",target = "participants")
    @Mapping(source = "lastMessageAt",target = "lastMessageAt")
    ChannelDto toDto(Channel channel, List<UserDto> userDtoList, Instant lastMessageAt);

}
