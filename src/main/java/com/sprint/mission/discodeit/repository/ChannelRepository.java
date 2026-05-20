package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.jcf.JCFSelectFilter;

import java.util.ArrayList;
import java.util.UUID;

public interface ChannelRepository {
    void create(String name, String description, ChannelType type);
    ArrayList<Channel> select(JCFSelectFilter fn);
    void update(UUID cnl, String name, String description, ChannelType type);
    void delete(UUID id);
}
