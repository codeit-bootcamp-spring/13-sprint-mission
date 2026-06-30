package com.sprint.mission.discodeit.dto.channel;

import com.sprint.mission.discodeit.entity.Channel;
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
  private List<UUID> participantIds; // 제공된 API 스펙에 맞추어 변경

  public static ChannelDto from(Channel channel, Instant lastMessageAt,
      List<UUID> participantsIds) {
    return ChannelDto.builder()
        .id(channel.getId())
        .type(channel.getType())
        .name(channel.getName())
        .description(channel.getDescription())
        .lastMessageAt(lastMessageAt)
        .participantIds(participantsIds)
        .build(); // PRIVATE 채널인 경우, 참여한 user의 id 정보 포함
  }
}
