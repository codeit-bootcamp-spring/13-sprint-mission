package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.Optional;

public interface ChannelRepository {

    void save();
    boolean existsChannelByName(String name);
    void createChannel(Channel channel);
    Optional<Channel> findChannelByName(String name);
    List<Channel> findAll();
    void deleteChannel(Channel channel);
}
