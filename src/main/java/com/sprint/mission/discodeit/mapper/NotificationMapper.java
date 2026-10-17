package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "receiverId", source = "receiver.id")
    NotificationDto toDto(Notification notification);

    List<NotificationDto> toDtoList(List<Notification> notifications);
}
