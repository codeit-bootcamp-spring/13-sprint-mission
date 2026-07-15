package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {BinaryContentMapper.class})
public interface UserMapper {

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "profile", source = "user.profile")
    @Mapping(target = "online", expression = "java(userStatus != null && userStatus.isOnline())")
    UserDto toDto(User user, UserStatus userStatus);
}
