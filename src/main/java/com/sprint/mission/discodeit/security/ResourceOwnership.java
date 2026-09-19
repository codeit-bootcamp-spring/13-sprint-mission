package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.repository.MessageRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("resourceOwnership")
@RequiredArgsConstructor
public class ResourceOwnership {

  private final MessageRepository messageRepository;

  public boolean isMessageAuthor(UUID messageId, Object principal) {
    if (!(principal instanceof DiscodeitUserDetails userDetails)) {
      return false;
    }

    return messageRepository.existsByIdAndAuthorId(
        messageId, userDetails.getUserDto().id());
  }
}
