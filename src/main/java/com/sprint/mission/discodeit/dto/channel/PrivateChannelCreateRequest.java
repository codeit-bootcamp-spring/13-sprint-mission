package com.sprint.mission.discodeit.dto.channel;

import io.swagger.v3.oas.annotations.media.Schema;
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

  private List<UUID> participantIds; // 제공된 API 스펙과 맞추어 수정
}
