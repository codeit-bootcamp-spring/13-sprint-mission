package com.sprint.mission.discodeit.dto.request.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

//메시지 생성 요청 정보를 전달하기 위한 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreateRequest {

  private String content; //사용자가 작성한 메시지 내용
  private UUID channelId; //어떤 채널에 메시지를 저장할 것인지 식별하기 위해 사용됨.
  private UUID authorId; //메시지 작성자(user)를 식별하기 위해 사용됨.
}
