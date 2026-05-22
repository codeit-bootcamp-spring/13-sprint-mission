package com.sprint.mission.discodeit.service.repository;

import com.sprint.mission.discodeit.entity.*;

import java.util.*;

public interface ChannelRepository {

    public void save(Channel channel);

    public Channel findById(UUID id);

    public List<Channel> findAll();

    public void deleteById(UUID id);

    public void update(Channel channel);
}
