package com.sprint.mission.discodeit.dto.binarycontent;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@AllArgsConstructor
public class BinaryContentCreateRequest {

  private String fileName;
  private String contentType;
  private byte[] bytes;
}
