package com.sprint.mission.discodeit.dto.readstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
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

  @NotNull(message = "사용자 ID는 필수입니다.")
  private UUID userId;

  @NotNull(message = "채널 ID는 필수입니다.")
  private UUID channelId;

  @NotNull(message = "마지막으로 읽은 시간은 필수입니다.")
  @PastOrPresent(message = "마지막으로 읽은 시간은 현재 또는 과거 시간이어야 합니다.")
  private Instant lastReadAt;
}
