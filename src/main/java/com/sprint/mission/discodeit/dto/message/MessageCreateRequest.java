package com.sprint.mission.discodeit.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

  // 텍스트 없이 첨부파일만 보내는 메시지 또한 허용
  @Size(max = 1024, message = "메시지 내용은 1024를 초과할 수 없습니다.")
  private String content;

  @NotNull(message = "채널 ID는 필수입니다.")
  private UUID channelId;

  @NotNull(message = "작성자 ID는 필수입니다.")
  private UUID authorId;
}
