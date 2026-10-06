package com.sprint.mission.discodeit.mapper;


import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import org.mapstruct.Mapper;

@Mapper
public interface NotificationMapper {

    NotificationDto toDto(Notification noty);
}
