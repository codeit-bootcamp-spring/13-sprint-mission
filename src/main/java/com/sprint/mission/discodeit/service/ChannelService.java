package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

  // Channel create(PublicChannelCreateRequest request);
  // Request DTO 대신 필요한 값만 직접 받음
  Channel create(String name, String description);

  // Channel create(PrivateChannelCreateRequest request);
  // 참여자 ID 목록만 받음
  Channel create(List<UUID> participantIds);

  ChannelDto find(UUID channelId);

  List<ChannelDto> findAllByUserId(UUID userId);

  // Channel update(UUID channelId, PublicChannelUpdateRequest request);
  // 필요한 값만 직접 받음
  Channel update(UUID channelId, String newName, String newDescription);

  void delete(UUID channelId);
}