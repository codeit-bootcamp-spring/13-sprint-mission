package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.CreateChannelRequest;
import com.sprint.mission.discodeit.dto.UpdateChannelRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

    ChannelDto create(CreateChannelRequest request);

    ChannelDto find(UUID id);

    List<ChannelDto> findAll();

    ChannelDto update(
            UUID id,
            UpdateChannelRequest request
    );

    void delete(UUID id);
}