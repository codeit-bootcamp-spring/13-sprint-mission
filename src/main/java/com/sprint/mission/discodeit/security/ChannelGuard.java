package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.basic.ChannelReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChannelGuard {
    private final ChannelReader channelReader;

    public boolean isChannelPrivate(UUID channelId) {
        return getChannel(channelId).isPrivate();
    }

    private Channel getChannel(UUID channelId) {
        return channelReader.getChannel(channelId);
    }

    public boolean isAccess(UUID channelId, UUID userId) {
        if (channelId == null || userId == null) {
            return false;
        }
        return channelReader.isChannelAccessible(channelId, userId);
    }
}
