package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import org.mapstruct.*;

import java.util.*;

@Mapper(componentModel = "spring", uses = {BinaryContentMapper.class})
public interface UserMapper {

    UserDto toDto(User user);

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "profile", source = "user.profile")// BinaryContentMapper가 자동 적용됨
    @Mapping(target = "online", expression = "java(userStatus != null && userStatus.isOnline())")
    UserDto toDto(User user, UserStatus userStatus);

    LoginResponse toLoginResponse(User user);

    List<UserDto> toDtoList(List<User> users);

}

