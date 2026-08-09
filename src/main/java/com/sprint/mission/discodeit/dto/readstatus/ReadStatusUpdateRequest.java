package com.sprint.mission.discodeit.dto.readstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Setter
@Getter
@ToString
@Schema(description = "수정할 읽음 상태 정보")
public class ReadStatusUpdateRequest {

  @NotNull(message = "읽음 상태 정보는 필수입니다.")
  @PastOrPresent(message = "마지막으로 읽은 시간은 현재 또는 과거 시간이어야 합니다.")
  private Instant newLastReadAt;
}
