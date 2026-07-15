package com.sprint.mission.discodeit.dto.readstatus;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ReadStatusDto {

  private UUID id;
  private UUID channelId;
  private UUID userId;
  private Instant lastReadAt; // 제공된 클래스 다이어그램에 맞춰 이름 변경
}
