package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;

import java.util.Collection;
import java.util.UUID;

public interface ChannelService {

	ChannelDto createPublic(PublicChannelCreateRequest request);
	ChannelDto createPrivate(PrivateChannelCreateRequest request);
	ChannelDto findById(UUID id);
	Collection<ChannelDto> findAllByUserId(UUID userId);
	ChannelDto update(UUID channelId, ChannelUpdateRequest request);
	void delete(UUID id);
	
}
