package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.User;

import java.util.Collection;
import java.util.UUID;

public interface ChannelService {

	ChannelResponse createPublic(PublicChannelCreateRequest request);
	ChannelResponse createPrivate(PrivateChannelCreateRequest request);
	ChannelResponse findById(UUID id);
	Collection<ChannelResponse> findAllByUserId(UUID userId);
	ChannelResponse update(ChannelUpdateRequest request);
	void delete(UUID id);
	
}
