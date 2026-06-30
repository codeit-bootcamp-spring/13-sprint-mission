package com.sprint.mission.discodeit.dto.binarycontent;

import com.sprint.mission.discodeit.entity.BinaryContent;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BinaryContentResponse { // record 타입은 실무에서 많이 사용되지 않기 때문에 전통적인 방식으로 DTO 선언

  private String fileName;
  private String contentType;
  private Long fileSize;

  public static BinaryContentResponse from(BinaryContent binaryContent) {
    return BinaryContentResponse.builder()
        .fileName(binaryContent.getFileName())
        .contentType(binaryContent.getContentType())
        .fileSize(binaryContent.getSize())
        .build();
  }
}
