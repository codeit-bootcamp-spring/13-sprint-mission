package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.CreateChannelRequest;
import com.sprint.mission.discodeit.dto.UpdateChannelRequest;
import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

    Channel create(CreateChannelRequest request);

    Channel find(UUID id);

    List<Channel> findAll();

    Channel update(UUID id, UpdateChannelRequest request);

    void delete(UUID id);
}