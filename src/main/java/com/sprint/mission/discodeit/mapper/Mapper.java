package com.sprint.mission.discodeit.mapper;


import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import org.mapstruct.ReportingPolicy;

@org.mapstruct.Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface Mapper {

    ReadStatusDto toDto(ReadStatus readStatus);
    UserStatusDto toDto(UserStatus userStatus);
    UserDto toDto(User user);
}
