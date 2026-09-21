package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("channelSecurity")
@RequiredArgsConstructor
public class ChannelSecurity {

    private final ChannelRepository channelRepository;

    public boolean isPrivateChannel(UUID channelId) {

        if (channelId == null) {
            return false;
        }

        return channelRepository.findById(channelId)
                .map(channel ->
                        channel.getType() == ChannelType.PRIVATE
                )
                .orElse(false);
    }
}