package com.sprint.mission.discodeit.dto.readstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Setter
@Getter
@ToString
@Schema(description = "수정할 읽음 상태 정보")
public class ReadStatusUpdateRequest {

  private Instant newLastReadAt;
}
