package com.sprint.mission.discodeit.dto.request.channel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

//공개 채널 수정 요청 정보를 전달하기 위한 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PublicChannelUpdateRequest {

  private String newName; //수정할 새로운 채채널 이름
  private String newDescription; //수정할 새로운 채널 설명
}
