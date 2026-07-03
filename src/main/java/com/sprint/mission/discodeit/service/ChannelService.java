package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.response.ChannelDto;
import java.util.List;
import java.util.UUID;

public interface ChannelService {

  ChannelDto createPublicChannel(PublicChannelCreateRequest request);

  ChannelDto createPrivateChannel(PrivateChannelCreateRequest request);

  List<ChannelDto> findAllByUserId(UUID userId);

  ChannelDto update(UUID id, ChannelUpdateRequest request);

  void delete(UUID id);

}
