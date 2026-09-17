package com.sprint.mission.discodeit.mapper;


import com.sprint.mission.discodeit.dto.projection.ChannelProjection;
import com.sprint.mission.discodeit.dto.projection.UserProjection;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import org.mapstruct.*;

import java.time.Instant;
import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
//        imports = {
//                MapperMethod.class
//        }

)
public interface MapStructMapper {


    // todo - 파일접근 매서드 어댑터화 ( 스태틱으로 파일에 자체 접근 할 수 있도록 변경.)
    // projection password 사용 안함
    @Mapping(source = "profileId", target = "id")
    @Mapping(source = "size", target = "size")
    @Mapping(source = "contentType", target = "contentType")
    @Mapping(target = "bytes", expression = "java(mapperMethod.getByteFrom(userProjection.profileId()))")
    BinaryContentDto toDto(UserProjection userProjection, @Context MapperMethod mapperMethod);


    @Mapping(source = "projection.id", target = "id")
    @Mapping(source = "projection.type", target = "type")
    @Mapping(source = "projection.name", target = "name")
    @Mapping(source = "projection.description", target = "description")
//    @Mapping(source = "projection.role", target= "role")
    @Mapping(source = "users", target = "participants")
    @Mapping(source = "projection.lastMessageAt", target = "lastMessageAt")
    ChannelDto toDto(ChannelProjection projection, List<UserDto> users);


    @Mapping(source = "user.id",target = "userId")
    @Mapping(source = "channel.id",target = "channelId")
    ReadStatusDto toDto(ReadStatus readStatus);

    @Mapping(source = "user.id",target = "userId")
    UserStatusDto toDto(UserStatus userStatus);




    @Mapping(source = "projection.id", target = "id")
    @Mapping(source = "projection.username", target = "username")
    @Mapping(source = "projection.email", target = "email")
    @Mapping(source = "profile", target = "profile")
    @Mapping(source = "projection.online", target = "online")
    UserDto toDto(UserProjection projection, BinaryContentDto profile);


    @Mapping(source = "message.id",target = "id")
    @Mapping(source = "message.channel.id",target = "channelId")
    @Mapping(source = "userDto",target = "author")
    @Mapping(source = "attachments",target = "attachments")
    MessageDto toDto(Message message, UserDto userDto, List<BinaryContentDto> attachments);



    // todo - userDto 매퍼 생성 후 삭제.
    @Mapping(source = "user.id",target = "id")
    @Mapping(source = "binaryContentDto", target="profile")
    @Mapping(source = "online", target = "online")
    UserDto toDto(User user, BinaryContentDto binaryContentDto, boolean online);
    // todo - 매퍼 매서드 삭제 예정.
    @Mapping(source = "userDtoList",target = "participants")
    @Mapping(source = "lastMessageAt",target = "lastMessageAt")
    ChannelDto toDto(Channel channel, List<UserDto> userDtoList, Instant lastMessageAt);
    // todo - 아래 매퍼 매서드로 변경. 추후 삭제.
    @Mapping(source = "bytes",target = "bytes")
    BinaryContentDto toDto(BinaryContent binaryContent,byte[] bytes);

}
