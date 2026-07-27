package com.sprint.mission.discodeit.dto.readstatus;

import com.sprint.mission.discodeit.entity.ReadStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ReadStatusResponse {

  private UUID id;
  private UUID channelId;
  private UUID userId;
  private Instant readAt;

  public static ReadStatusResponse from(ReadStatus readStatus) {
    return ReadStatusResponse.builder()
        .id(readStatus.getId())
        .channelId(readStatus.getChannel().getId())
        .userId(readStatus.getUser().getId())
        .readAt(readStatus.getLastReadAt())
        .build();
  }
}
