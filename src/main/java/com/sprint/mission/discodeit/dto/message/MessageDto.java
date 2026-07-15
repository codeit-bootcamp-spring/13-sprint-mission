package com.sprint.mission.discodeit.dto.message;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.dto.user.UserDto;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class MessageDto {

  private UUID id;
  Instant createdAt;
  Instant updatedAt;
  private UUID channelId;
  private UserDto author;
  private String content;
  private List<BinaryContentDto> attachments; // 제공된 클래스 다이어그램에 맞춰 추가
}
