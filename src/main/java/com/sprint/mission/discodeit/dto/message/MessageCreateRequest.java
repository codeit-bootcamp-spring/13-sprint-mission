package com.sprint.mission.discodeit.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Setter
@Getter
@ToString
@AllArgsConstructor
@Schema(description = "Message 생성 정보")
public class MessageCreateRequest {

  private String content;
  private UUID channelId;
  private UUID authorId;
}
