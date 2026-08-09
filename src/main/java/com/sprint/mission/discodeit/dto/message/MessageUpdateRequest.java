package com.sprint.mission.discodeit.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@AllArgsConstructor
@Schema(description = "수정할 Message 내용")
public class MessageUpdateRequest {

  @NotBlank(message = "수정할 메시지 내용은 필수입니다.")
  @Size(max = 1024, message = "수정할 메시지 내용은 1024를 초과할 수 없습니다.")
  private String newContent;
}
