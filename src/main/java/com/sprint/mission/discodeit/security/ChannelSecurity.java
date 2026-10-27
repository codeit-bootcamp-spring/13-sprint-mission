package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChannelSecurity {

  private final ChannelRepository channelRepository;
  private final ReadStatusRepository readStatusRepository;

  public boolean isPrivate(UUID channelId) {
    return channelRepository.findById(channelId)
        .map(channel -> channel.getType() == ChannelType.PRIVATE)
        .orElse(false);
  }

  public boolean canAccess(UUID channelId, UUID userId) {
    return channelRepository.findById(channelId)
        .map(channel ->
            channel.getType() == ChannelType.PUBLIC
                || readStatusRepository
                .findByUserIdAndChannelId(userId, channelId)
                .isPresent()
        )
        .orElse(false);
  }
}
