package com.sprint.mission.discodeit.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

//사용자 상태 생성 요청 정보를 전달하기 위한 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusCreateRequest {

  private UUID userId; //어떤 사용자의 상태 정보인지 구분하기 위해 사용됨.
  private Instant lastActiveAt; //사용자의 마지막 활동 시각
}
