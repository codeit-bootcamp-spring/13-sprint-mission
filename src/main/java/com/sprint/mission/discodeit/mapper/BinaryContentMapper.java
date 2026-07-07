package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@NoArgsConstructor
public class BinaryContentMapper {

    public BinaryContentDto toDto(BinaryContent binaryContent) {
        return new BinaryContentDto(
                binaryContent.getId()
                , binaryContent.getFileName()
                , binaryContent.getSize()
                , binaryContent.getContentType()
                , binaryContent.getBytes()
        );
    }

    public BinaryContentDto toDto(BinaryContent binaryContent,byte[] content) {
        return new BinaryContentDto(
                binaryContent.getId()
                , binaryContent.getFileName()
                , binaryContent.getSize()
                , binaryContent.getContentType()
                , content
        );
    }

}
