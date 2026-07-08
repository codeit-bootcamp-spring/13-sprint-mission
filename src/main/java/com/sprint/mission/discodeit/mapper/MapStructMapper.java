package com.sprint.mission.discodeit.mapper;


import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;
import java.util.List;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MapStructMapper {

    @Mapping(source = "bytes",target = "bytes")
    BinaryContentDto toDto(BinaryContent binaryContent,byte[] bytes);


    @Mapping(source = "userDtoList",target = "participants")
    @Mapping(source = "lastMessageAt",target = "lastMessageAt")
    ChannelDto toDto(Channel channel, List<UserDto> userDtoList, Instant lastMessageAt);

    @Mapping(source = "user.id",target = "userId")
    @Mapping(source = "channel.id",target = "channelId")
    ReadStatusDto toDto(ReadStatus readStatus);

    @Mapping(source = "user.id",target = "userId")
    UserStatusDto toDto(UserStatus userStatus);

    @Mapping(source = "user.id",target = "id")
    @Mapping(source = "binaryContentDto", target="profile")
    @Mapping(source = "online", target = "online")
    UserDto toDto(User user, BinaryContentDto binaryContentDto, boolean online);

    @Mapping(source = "message.id",target = "id")
    @Mapping(source = "message.channel.id",target = "channelId")
    @Mapping(source = "userDto",target = "author")
    @Mapping(source = "attachments",target = "attachments")
    MessageDto toDto(Message message, UserDto userDto, List<BinaryContentDto> attachments);


}
