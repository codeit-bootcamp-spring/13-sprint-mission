package com.sprint.mission.discodeit.dto.userstatus;

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
@Schema(description = "변경할 User 온라인 상태 정보")
public class UserStatusUpdateRequest {

  @NotNull(message = "변경된 마지막 접속 시간은 필수입니다.")
  @PastOrPresent(message = "변경된 마지막 접속 시간은 현재 또는 과거 시간이어야 합니다.")
  private Instant newLastActiveAt;
}
