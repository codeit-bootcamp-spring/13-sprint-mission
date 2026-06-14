package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import java.util.List;
import java.util.Optional;

public interface ChannelService {
    void save(Channel channel);
    Optional<Channel> findById(String id);
    List<Channel> findAll();
    void update(Channel channel);
    void delete(String id);
}