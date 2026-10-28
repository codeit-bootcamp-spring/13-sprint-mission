package com.sprint.mission.discodeit.realtime;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ChannelAccess {
  private final ChannelRepository channels;

  @Transactional(readOnly = true)
  public boolean canRead(UUID channelId, Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof DiscodeitUserDetails user)) {
      return false;
    }
    return channels.findById(channelId).map(channel -> switch (channel.getType()) {
      case PUBLIC -> true;
      case PRIVATE -> channel.getAllowedUserList().contains(user.getUserDto().id());
      case MANAGER -> user.getUserDto().role() != Role.USER;
    }).orElse(false);
  }
}
