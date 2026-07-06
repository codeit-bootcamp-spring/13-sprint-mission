package com.sprint.mission.discodeit.dto.userstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Setter
@Getter
@ToString
@Schema(description = "변경할 User 온라인 상태 정보")
public class UserStatusUpdateRequest {

  private Instant newLastActiveAt;
}
