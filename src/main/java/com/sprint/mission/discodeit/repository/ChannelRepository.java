package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.Optional;

public interface ChannelRepository {

    void save();
    void createChannel(Channel channel);
    Optional<Channel> findChannel(Channel channel);
    List<Channel> findAll();
    void deleteChannel(Channel channel);
}
