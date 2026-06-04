package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.*;

import java.util.*;

public interface ChannelRepository {

    void create(Channel channel);

    Channel read(UUID id);

    List<Channel> readAll();

    void update(UUID id, Channel channel);

    void delete(UUID id);

    boolean exists(UUID id);
}
