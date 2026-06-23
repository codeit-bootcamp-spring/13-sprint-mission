package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public interface ChannelRepository {
    void save(Channel channel);
    List<Channel> find(Predicate<Channel> fn);
    List<Channel> findAll();
    Optional<Channel> findById(UUID id);
    void delete(UUID id);
}
