package com.sprint.mission.discodeit.repository;


import com.sprint.mission.discodeit.entity.Channel;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChannelRepository {
    Channel createOne(Channel channel) throws IOException;

    Optional<Channel> readOne(UUID id) throws IOException;

    List<Channel> readAll() throws IOException;

    void deleteOne(UUID id) throws IOException;
}
