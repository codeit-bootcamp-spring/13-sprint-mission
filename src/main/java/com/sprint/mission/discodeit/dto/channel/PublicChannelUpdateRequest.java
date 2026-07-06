package com.sprint.mission.discodeit.dto.channel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@AllArgsConstructor
@Schema(description = "수정할 Channel 정보")
public class PublicChannelUpdateRequest { // 제공된 API 스펙에 맞추어 변경

  private String newName;
  private String newDescription;
}
