package com.sprint.mission.discodeit.dto.channel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@ToString
@Schema(description = "Private Channel 생성 정보")
public class PrivateChannelCreateRequest {

  @NotNull(message = "비공개 채널의 참여자는 필수입니다.")
  @Size(min = 1, message = "비공개 채널을 생성하기 위해 최소 한 명의 회원이 참여해야 합니다.")
  private List<UUID> participantIds; // 제공된 API 스펙과 맞추어 수정
}
