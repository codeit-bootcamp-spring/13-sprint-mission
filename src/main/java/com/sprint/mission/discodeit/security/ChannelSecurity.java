package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component("channelSecurity")
@RequiredArgsConstructor
@Slf4j
public class ChannelSecurity {

    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;

    @Transactional(readOnly = true)
    public boolean canAccess(UUID channelId, Authentication authentication) {
        if(authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        if (!((authentication.getPrincipal())
        instanceof DiscodeitUserDetails userDetails)) {
            return false;
        }

        UUID userId = userDetails.getUserDto().id();

        Channel channel = channelRepository.findById(channelId).orElse(null);

        if(channel == null) {
            return false;
        }

        if(channel.getType() == ChannelType.PUBLIC) {
            return true;
        }


        if (channel.getType() == ChannelType.PRIVATE) {
            return readStatusRepository
                    .findByChannelIdAndUserId(channelId, userId)
                    .isPresent();
        }

        return false;
    }

}


