package com.sprint.mission.discodeit.dto.userstatus;

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
public class UserStatusCreateRequest {

  @NotNull(message = "사용자 ID는 필수입니다.")
  private UUID userId;

  @NotNull(message = "마지막 접속 시간은 필수입니다.")
  @PastOrPresent(message = "마지막 접속 시간은 현재 또는 과거 시간이어야 합니다.")
  private Instant lastActiveAt;
}
