package com.sprint.mission.discodeit.dto.message;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class MessageResponse {

  private Channel channel;
  private User author;
  private String content;
  private List<BinaryContent> attachments;

  public static MessageResponse from(Message message) {
    return MessageResponse.builder()
        .channel(message.getChannel())
        .author(message.getAuthor())
        .content(message.getContent())
        .attachments(message.getAttachments())
        .build();
  }
}
