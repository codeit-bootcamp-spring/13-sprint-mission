package com.sprint.mission.discodeit.dto.channel;

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
@Schema(description = "Public Channel 생성 정보")
public class PublicChannelCreateRequest {

  @NotBlank(message = "채널 이름은 필수입니다.")
  @Size(max = 100, message = "채널 이름은 100자 이하만 가능합니다.")
  private String name;

  @Size(max = 500, message = "채널에 대한 설명은 500자를 초과할 수 없습니다.")
  private String description; // 기존 로직 유지
}
