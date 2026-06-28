package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.response.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

  ChannelDto createPublic(String name, String description);

  ChannelDto createPrivate(List<UUID> participantIds);

  ChannelDto find(UUID channelId);

  List<ChannelDto> findAllByUserId(UUID userId);

  ChannelDto update(UUID channelId, String newName, String newDescription);

  void delete(UUID channelId);
}