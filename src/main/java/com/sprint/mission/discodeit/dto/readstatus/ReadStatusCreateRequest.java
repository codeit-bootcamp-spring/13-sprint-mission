package com.sprint.mission.discodeit.dto.readstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@ToString
@Schema(description = "Message 읽음 상태 생성 정보")
public class ReadStatusCreateRequest {

  private UUID userId;
  private UUID channelId;
  private Instant lastReadAt;
}
