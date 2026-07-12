package com.sprint.mission.discodeit.dto.binarycontent;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BinaryContentDto { // record 타입은 실무에서 많이 사용되지 않기 때문에 전통적인 방식으로 DTO 선언

  private UUID id;
  private String fileName;
  private String contentType;
  private Long size;
  private byte[] bytes; // 제공된 클래스 다이어그램에 맞춰 추가
}
