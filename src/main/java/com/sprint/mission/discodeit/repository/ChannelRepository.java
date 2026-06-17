package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.*;

import java.util.*;

public interface ChannelRepository {

    void create(Channel channel);

    Channel find(UUID id);

    List<Channel> findAll();

    void update(UUID id, Channel channel);

    void delete(UUID id);

    boolean exists(UUID id);
}
