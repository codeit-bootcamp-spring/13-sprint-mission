package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserStatusMapper {
    UserStatusDto toDto(UserStatus userStatus);
}
