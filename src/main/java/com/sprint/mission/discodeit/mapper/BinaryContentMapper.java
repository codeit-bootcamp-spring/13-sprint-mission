package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import org.springframework.stereotype.Component;

@Component
public class BinaryContentMapper {

  // 엔티티를 DTO로 매핑하는 로직 책임지는 Mapper 컴포넌트 정의
  public BinaryContentDto toDto(BinaryContent binaryContent) {
    return BinaryContentDto.builder()
        .id(binaryContent.getId())
        .fileName(binaryContent.getFileName())
        .contentType(binaryContent.getContentType())
        .size(binaryContent.getSize())
        .bytes(binaryContent.getBytes())
        .build();
  }
}
