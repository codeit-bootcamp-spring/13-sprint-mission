package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.ReadStatusResponse;
import com.sprint.mission.discodeit.entity.ReadStatus;
import org.springframework.stereotype.Component;

@Component
public class ReadStatusMapper {

    public ReadStatusResponse toDto(ReadStatus entity) {
        if (entity == null) {
            return null;
        }

        return new ReadStatusResponse(
                entity.getId(),
                entity.getChannel().getId(),
                entity.getUser().getId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getLastReadAt()
        );
    }
}