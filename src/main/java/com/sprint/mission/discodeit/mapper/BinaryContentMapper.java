package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import org.springframework.stereotype.Component;

@Component
public class BinaryContentMapper {

    public BinaryContentResponse toDto(BinaryContent binaryContent) {
        if (binaryContent == null) {
            return null;
        }

        return new BinaryContentResponse(
                binaryContent.getId(),
                binaryContent.getCreatedAt(),
                binaryContent.getFileName(),
                binaryContent.getSize(),
                binaryContent.getContentType(),
                binaryContent.getBytes()
        );
    }
}