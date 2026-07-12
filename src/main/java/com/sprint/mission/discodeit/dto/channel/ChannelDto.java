package com.sprint.mission.discodeit.dto.channel;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.entity.ChannelType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ChannelDto {

  private UUID id;
  private ChannelType type;
  private String name;
  private String description;
  private Instant lastMessageAt;
  private List<UserDto> participants; // 제공된 클래스 다이어그램에 맞추어 변경
}
