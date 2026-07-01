package com.sprint.mission.discodeit.dto.request.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

//메시지 수정 요청 정보를 전달하기 위한 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageUpdateRequest {

  private String newContent; //수정될 새로운 메시지 내용
}
