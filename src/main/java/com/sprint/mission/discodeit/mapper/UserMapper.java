package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {BinaryContentMapper.class})
public interface UserMapper {

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "profile", source = "user.profile")// BinaryContentMapper가 자동 적용됨
    @Mapping(target = "role", source = "user.role")
    @Mapping(target = "online", source = "online")
    UserDto toDto(User user, Boolean online);

}

