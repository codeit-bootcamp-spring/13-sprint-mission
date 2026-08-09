package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import org.mapstruct.*;

import java.util.*;

@Mapper(componentModel = "spring")
public interface UserStatusMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "lastActiveAt", source = "lastActivityAt")
    UserStatusDto toDto(UserStatus userStatus);

    List<UserStatusDto> toDtoList(List<UserStatus> userStatuses);
}
