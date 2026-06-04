package com.sprint.mission.discodeit.repository;


import com.sprint.mission.discodeit.entity.Channel;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChannelRepository {
    Channel saveChannel(Channel channel) throws IOException;

    Optional<Channel> fineChannel(UUID id) throws IOException;

    List<Channel> findChannels() throws IOException;

    void deleteChannel(UUID id) throws IOException;
}
