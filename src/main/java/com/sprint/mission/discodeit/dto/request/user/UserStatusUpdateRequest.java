package com.sprint.mission.discodeit.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

//사용자 상태 수정 요청 정보를 전달하기 위한 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusUpdateRequest {

  private Instant newLastActiveAt; //수정할 새로운(최근) 마지막 활동 시각
}
